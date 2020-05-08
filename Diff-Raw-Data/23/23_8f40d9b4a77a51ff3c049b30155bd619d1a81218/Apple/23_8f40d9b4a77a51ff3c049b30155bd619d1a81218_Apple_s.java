 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;
 import com.dmdirc.events.ClientOpenedEvent;
 import com.dmdirc.events.UserErrorEvent;
 import com.dmdirc.interfaces.ConnectionManager;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.util.InvalidURIException;
 import com.dmdirc.util.URIParser;
 
 import java.awt.Image;
 import java.awt.PopupMenu;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EventObject;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.UIManager;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Integrate DMDirc with OS X better.
  */
 @Singleton
 public class Apple implements InvocationHandler {
 
     /** Store any addresses that are opened before CLIENT_OPENED. */
     private final Collection<URI> addresses = new ArrayList<>();
     /** Config manager used to read settings. */
     private final AggregateConfigProvider configManager;
     /** The "Application" object used to do stuff on OS X. */
     private Object application;
     /** Whether we're listening or not. */
     private boolean isListener;
     /** The MenuBar for the application. */
     private MenuBar menuBar;
     /** Whether the CLIENT_OPENED action has been called or not. */
     private boolean clientOpened;
     /** The server manager to use to connect to URLs. */
     private final ConnectionManager connectionManager;
     /** Event bus. */
     private final DMDircMBassador eventBus;
 
     /**
      * Creates a new instance of {@link Apple}.
      *
      * <p>
      * This will attempt to load the native library and register the URL open callback.
      *
      * @param configManager Config manager
      * @param connectionManager The server manager to use to connect to URLs.
      * @param eventBus      The bus to listen for events on.
      */
     @Inject
     public Apple(
             @GlobalConfig final AggregateConfigProvider configManager,
             final ConnectionManager connectionManager,
             final DMDircMBassador eventBus) {
         this.configManager = configManager;
         this.connectionManager = connectionManager;
         this.eventBus = eventBus;
     }
 
     public void load() {
         if (isApple()) {
             try {
                 System.loadLibrary("DMDirc-Apple"); // NOPMD
                 registerOpenURLCallback();
                 eventBus.subscribe(this);
             } catch (UnsatisfiedLinkError ule) {
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM,
                         ule, "Unable to load JNI library", ""));
             }
         }
     }
 
     /**
      * Register the getURL Callback.
      *
      * @return 0 on success, 1 on failure.
      */
     private synchronized native int registerOpenURLCallback();
 
     /**
      * Call a method on the given object.
      *
      * @param obj        Object to call method on.
      * @param className  Name of class that object really is.
      * @param methodName Method to call
      * @param classes    Array of classes to pass when calling getMethod
      * @param objects    Array of objects to pass when invoking.
      *
      * @return Output from method.invoke()
      */
     private Object reflectMethod(final Object obj, final String className, final String methodName,
            final Class<?>[] classes, final Object... objects) {
         try {
             final Class<?> clazz = className == null ? obj.getClass() : Class.forName(className);
             final Method method = clazz.getMethod(methodName, classes == null ? new Class<?>[0]
                     : classes);
             return method.invoke(obj, objects == null ? new Object[0] : objects);
         } catch (ReflectiveOperationException ex) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, ex, "Unable to find OS X classes.", ""));
         }
 
         return null;
     }
 
     /**
      * Handle a method call to the apple Application class.
      *
      * @param methodName Method to call
      * @param classes    Array of classes to pass when calling getMethod
      * @param objects    Array of objects to pass when invoking.
      *
      * @return Output from method.invoke()
      */
     private Object doAppleMethod(final String methodName, final Class<?>[] classes,
            final Object... objects) {
         if (!isApple()) {
             return null;
         }
 
         return reflectMethod(getApplication(), null, methodName, classes, objects);
     }
 
     /**
      * Get the "Application" object.
      *
      * @return Object that on OSX will be an "Application"
      */
     public Object getApplication() {
         synchronized (Apple.class) {
             if (isApple() && application == null) {
                 application = reflectMethod(null, "com.apple.eawt.Application", "getApplication",
                         null, null);
             }
             return application;
         }
     }
 
     /**
      * Are we on OS X?
      *
      * @return true if we are running on OS X
      */
     public static boolean isApple() {
         return System.getProperty("os.name").contains("OS X");
     }
 
     /**
      * Are we using the OS X look and feel?
      *
      * @return true if we are using the OS X look and feel
      */
     public static boolean isAppleUI() {
         final String name = UIManager.getLookAndFeel().getClass().getName();
         return isApple() && ("apple.laf.AquaLookAndFeel".equals(name)
                 || "com.apple.laf.AquaLookAndFeel".equals(name));
     }
 
     /**
      * Set some OS X only UI settings.
      */
     public void setUISettings() {
         if (!isApple()) {
             return;
         }
 
         // Set some Apple OS X related stuff from http://tinyurl.com/6xwuld
         final String aaText = configManager.getOptionBool("ui", "antialias") ? "on" : "off";
 
         System.setProperty("apple.awt.antialiasing", aaText);
         System.setProperty("apple.awt.textantialiasing", aaText);
         System.setProperty("apple.awt.showGrowBox", "true");
         System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DMDirc");
         System.setProperty("apple.laf.useScreenMenuBar", "true");
         System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
         System.setProperty("com.apple.mrj.application.live-resize", "true");
     }
 
     /**
      * Requests this application to move to the foreground.
      *
      * @param allWindows if all windows of this application should be moved to the foreground, or
      *                   only the foremost one
      */
     public void requestForeground(final boolean allWindows) {
        doAppleMethod("requestForeground", new Class<?>[]{Boolean.TYPE}, allWindows);
     }
 
     /**
      * Requests user attention to this application (usually through bouncing the Dock icon).
      * Critical requests will continue to bounce the Dock icon until the app is activated. An
      * already active application requesting attention does nothing.
      *
      * @param isCritical If this is false, the dock icon only bounces once, otherwise it will bounce
      *                   until clicked on.
      */
     public void requestUserAttention(final boolean isCritical) {
        doAppleMethod("requestUserAttention", new Class<?>[]{Boolean.TYPE}, isCritical);
     }
 
     /**
      * Attaches the contents of the provided PopupMenu to the application's Dock icon.
      *
      * @param menu the PopupMenu to attach to this application's Dock icon
      */
     public void setDockMenu(final PopupMenu menu) {
        doAppleMethod("setDockMenu", new Class<?>[]{PopupMenu.class}, menu);
     }
 
     /**
      * Get the PopupMenu attached to the application's Dock icon.
      *
      * @return the PopupMenu attached to this application's Dock icon
      */
     public PopupMenu getDockMenu() {
         final Object result = doAppleMethod("getDockMenu", null, null);
         return result instanceof PopupMenu ? (PopupMenu) result : null;
     }
 
     /**
      * Changes this application's Dock icon to the provided image.
      *
      * @param image The image to use
      */
     public void setDockIconImage(final Image image) {
        doAppleMethod("setDockIconImage", new Class<?>[]{Image.class}, image);
     }
 
     /**
      * Obtains an image of this application's Dock icon.
      *
      * @return The application's dock icon.
      */
     public Image getDockIconImage() {
         final Object result = doAppleMethod("getDockIconImage", null, null);
         return result instanceof Image ? (Image) result : null;
     }
 
     /**
      * Affixes a small system provided badge to this application's Dock icon. Usually a number.
      *
      * @param badge textual label to affix to the Dock icon
      */
     public void setDockIconBadge(final String badge) {
        doAppleMethod("setDockIconBadge", new Class<?>[]{String.class}, badge);
     }
 
     /**
      * Sets the default menu bar to use when there are no active frames. Only used when the system
      * property "apple.laf.useScreenMenuBar" is "true", and the Aqua Look and Feel is active.
      *
      * @param menuBar to use when no other frames are active
      */
     public void setDefaultMenuBar(final JMenuBar menuBar) {
        doAppleMethod("setDefaultMenuBar", new Class<?>[]{JMenuBar.class}, menuBar);
     }
 
     /**
      * Add this application as a handler for the given event.
      *
      * @param handlerClass  Class used as the handler.
      * @param handlerMethod Method used to set the handler.
      *
      * @return True if we succeeded.
      */
     private boolean addHandler(final String handlerClass, final String handlerMethod) {
         try {
             final Class<?> listenerClass = Class.forName(handlerClass);
             final Object listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                     new Class<?>[]{listenerClass}, this);
 
             final Method method = getApplication().getClass().getMethod(handlerMethod,
                     listenerClass);
             method.invoke(getApplication(), listener);
 
             return true;
         } catch (ClassNotFoundException | NoSuchMethodException |
                 IllegalAccessException | InvocationTargetException ex) {
             return false;
         }
     }
 
     /**
      * Set this up as a listener for the Apple Events.
      *
      * @return True if the listener was added, else false.
      */
     public boolean setListener() {
         if (!isApple() || isListener) {
             return false;
         }
 
         addHandler("com.apple.eawt.OpenURIHandler", "setOpenURIHandler");
         addHandler("com.apple.eawt.AboutHandler", "setAboutHandler");
         addHandler("com.apple.eawt.QuitHandler", "setQuitHandler");
         addHandler("com.apple.eawt.PreferencesHandler", "setPreferencesHandler");
         isListener = true;
 
         return true;
     }
 
     @Override
     public Object invoke(final Object proxy, final Method method, final Object[] args)
             throws ReflectiveOperationException {
         if (!isApple()) {
             return null;
         }
 
         try {
             final Class<?>[] classes = new Class<?>[args.length];
 
             for (int i = 0; i < args.length; i++) {
                 if (EventObject.class.isInstance(args[i])) {
                     classes[i] = EventObject.class;
                 } else {
                     final Class<?> c = args[i].getClass();
                     if ("com.apple.eawt.QuitResponse".equals(c.getCanonicalName())) {
                         classes[i] = Object.class;
                     } else {
                         classes[i] = c;
                     }
                 }
             }
 
             final Method thisMethod = getClass().getMethod(method.getName(), classes);
             return thisMethod.invoke(this, args);
         } catch (final NoSuchMethodException e) {
             if ("equals".equals(method.getName()) && args.length == 1) {
                 return proxy == args[0];
             }
         }
 
         return null;
     }
 
     /**
      * Set the MenuBar. This will unset all menu mnemonics aswell if on the OSX ui.
      *
      * @param newMenuBar MenuBar to use to send events to,
      */
     public void setMenuBar(final MenuBar newMenuBar) {
         menuBar = newMenuBar;
         if (!isAppleUI()) {
             return;
         }
         for (int i = 0; i < menuBar.getMenuCount(); i++) {
             final JMenu menu = menuBar.getMenu(i);
             if (menu == null) {
                 continue;
             }
             menu.setMnemonic(0);
             for (int j = 0; j < menu.getItemCount(); j++) {
                 final JMenuItem menuItem = menu.getItem(j);
                 if (menuItem != null) {
                     menuItem.setMnemonic(0);
                 }
             }
         }
     }
 
     /**
      * Handle an event using the menuBar.
      *
      * @param name The name of the event according to the menubar
      */
     public void handleMenuBarEvent(final String name) {
         if (!isApple() || menuBar == null) {
             return;
         }
         final ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, name);
 
         for (int i = 0; i < menuBar.getMenuCount(); i++) {
             final JMenu menu = menuBar.getMenu(i);
             if (menu instanceof ActionListener) {
                 ((ActionListener) menu).actionPerformed(actionEvent);
             }
         }
     }
 
     /**
      * This is called when About is selected from the Application menu.
      *
      * @param event an ApplicationEvent object
      */
     public void handleAbout(final EventObject event) {
         handleMenuBarEvent("About");
     }
 
     /**
      * This is called when Preferences is selected from the Application menu.
      *
      * @param event an ApplicationEvent object
      */
     public void handlePreferences(final EventObject event) {
         handleMenuBarEvent("Preferences");
     }
 
     /**
      * Called when the client is opened for the first time.
      *
      * @param event The event describing the client opening.
      */
     @Handler
     public void handleClientOpened(final ClientOpenedEvent event) {
         synchronized (addresses) {
             clientOpened = true;
             addresses.forEach(connectionManager::connectToAddress);
             addresses.clear();
         }
     }
 
     /**
      * This is called when Quit is selected from the Application menu.
      *
      * @param event        an ApplicationEvent object
      * @param quitResponse QuitResponse object.
      */
     public void handleQuitRequestWith(final EventObject event, final Object quitResponse) {
         // Technically we should tell OS X if the quit succeeds or not, but we
         // have no way of knowing the result just yet.
         //
         // So instead we will just tell it that the quit was cancelled every
         // time, and then just quit anyway if we need to.
         reflectMethod(quitResponse, null, "cancelQuit", null, null);
 
         handleMenuBarEvent("Exit");
     }
 
     /**
      * Callback from our JNI library. This should work when not launcher via JavaApplicationStub
      *
      * @param url The irc url string to connect to.
      */
     public void handleOpenURL(final String url) {
         try {
             handleURI(new URIParser().parseFromText(url));
         } catch (final InvalidURIException ex) {
             // Do nothing?...
         }
     }
 
     /**
      * Callback from OSX Directly. This will work if we were launched using the JavaApplicationStub
      *
      * @param event Event related to this callback. This event will have a reflectable getURI method
      *              to get a URI.
      */
     public void openURI(final EventObject event) {
         if (!isApple()) {
             return;
         }
 
         final Object obj = reflectMethod(event, null, "getURI", null, null);
         if (obj instanceof URI) {
             handleURI((URI) obj);
         }
     }
 
     /**
      * Handle connecting to a URI.
      *
      * If called before the client has finished opening, the URI will be added to a list that will
      * be connected to once the CLIENT_OPENED action is called. Otherwise we connect right away.
      *
      * @param uri URI to connect to.
      */
     private void handleURI(final URI uri) {
         synchronized (addresses) {
             if (clientOpened) {
                 // When the JNI callback is called there is no
                 // ContextClassLoader set, which causes an NPE in
                 // IconManager if no servers have been connected to yet.
                 if (Thread.currentThread().getContextClassLoader() == null) {
                     Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
                 }
 
                 connectionManager.connectToAddress(uri);
             } else {
                 addresses.add(uri);
             }
         }
     }
 
 }
