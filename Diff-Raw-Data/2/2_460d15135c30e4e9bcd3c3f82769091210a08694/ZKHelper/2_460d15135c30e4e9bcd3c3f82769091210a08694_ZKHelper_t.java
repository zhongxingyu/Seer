 package cz.datalite.helpers;
 
 import cz.datalite.zk.annotation.ZkEvents;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.util.resource.Labels;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zul.*;
 import org.zkoss.zul.impl.NumberInputElement;
 
 import java.util.*;
 
 /**
  * Pomocne funkce pro ZK framework
  *
  * @author Jiri Bubnik
  */
 public class ZKHelper {
 
     /**
      * logger
      */
     private static final Logger LOGGER = LoggerFactory.getLogger(ZKHelper.class);
 
     /**
      * Checks, if parameter exists in arg map and returns its value (it might be null) or the default value.
      * Target class is required for type safety, result type is checked and if not matched, exception is thrown.
      *
      * @param <T>          type of parameter
      * @param arg          argument map
      * @param key          name of parameter
      * @param targetClass  requested type
      * @param defaultValue default value if the parameter is not set at all
      * @return value of the parameter or null
      */
     public static <T> T getOptionalParameter(Map arg, String key, Class<T> targetClass, T defaultValue) {
         if (!arg.containsKey(key))
             return defaultValue;
 
         Object val = arg.get(key);
 
         if (val == null)
             return null;
 
         // http param are automaticaly assigned to String[] - unwrap
         if (val.getClass().isArray() && !targetClass.isArray()) {
             if (((Object[]) val).length == 0)
                 return null;
             else
                 val = ((Object[]) val)[0];
         }
 
         // same class
         if (targetClass.isInstance(val))
             return (T) val;
 
         try {
             return TypeConverter.convertTo(val.toString(), targetClass);
         } catch (Exception ex) {
             throw new java.lang.IllegalArgumentException("Parameter '" + key + "' is not of expected type '" + targetClass.getName() +
                     "'. Unable assign value '" + arg.get(key) + "' of type '" + arg.get(key).getClass() + "'.");
         }
     }
 
     /**
      * Checks, that parameter exists in arg map and returns its value (it might be null).
      * Target class is required for type safety, result type is checked and if not matched, exception is thrown.
      *
      * @param <T>         type of parameter
      * @param arg         argument map
      * @param key         name of parameter
      * @param targetClass requested type
      * @return value of the parameter or null
      */
     public static <T> T getRequiredParameter(Map arg, String key, Class<T> targetClass) {
         if (!arg.containsKey(key))
             throw new java.lang.IllegalArgumentException("Mandatory parameter '" + key + "' is not set.");
 
         return getOptionalParameter(arg, key, targetClass, null);
     }
 
     /**
      * Get child component by component type (get children and try isAssignableFrom())
      *
      * @param comp  which component
      * @param clazz which class
      * @return component list of a type
      */
     public static <T> T findChildByClass(Component comp, Class<T> clazz) {
         if (clazz.isAssignableFrom(comp.getClass())) {
             return (T) comp;
         }
 
         for (Component c : comp.getChildren()) {
             T child = findChildByClass(c, clazz);
             if (child != null) {
                 return child;
             }
         }
 
         return null;
     }
 
     /**
      * Get child components by component type (get children and try isAssignableFrom())
      *
      * @param comp  which component
      * @param clazz which class
      * @return component list of a type
      */
     public static <T> List<T> findChildrenByClass(Component comp, Class<T> clazz) {
         List<T> ret = new LinkedList();
 
         for (Component c : comp.getChildren()) {
             if (clazz.isAssignableFrom(c.getClass())) {
                 ret.add((T) c);
             }
         }
 
         return ret;
     }
 
     /**
      * Recurse to children tree and try to find by Id (compares by equals comp.getId())
      *
      * @param comp        starting component (start with it and it's chidlren)
      * @param componentId id to find
      * @return found component or null
      */
     public static Component findChildById(Component comp, String componentId) {
         if (componentId.equals(comp.getId())) {
             return comp;
         }
 
         for (Component c : comp.getChildren()) {
             Component child = findChildById(c, componentId);
             if (child != null) {
                 return child;
             }
         }
         return null;
     }
 
     /**
      * Recurse to children tree and try to find by name (compares by equals comp.getDefinition().getName())
      *
      * @param comp        starting component (start with it and it's chidlren)
      * @param componentName name of the component to fund
      * @return found component or null
      */
     public static Component findChildByName(Component comp, String componentName) {
         if (componentName.equals(comp.getDefinition().getName())) {
             return comp;
         }
 
         for (Component c : comp.getChildren()) {
             Component child = findChildByName(c, componentName);
             if (child != null) {
                 return child;
             }
         }
         return null;
     }
 
     /**
      * Recurse to children tree and try to find by Id (compares by equals comp.getAttribute("dlId")).
      * use dlId when id is not possible because of binding (usually in list, where binding adds it's own ids)
      * in ZUL use as: <custom-attributes dlId="MojeHodnota"/>
      *
      * @param comp        component to start
      * @param componentId id to find
      * @return found component or null
      */
     public static Component findChildByDlId(Component comp, String componentId) {
         if (componentId.equals(comp.getAttribute("dlId"))) {
             return comp;
         }
 
         for (Component c : comp.getChildren()) {
             Component child = findChildByDlId(c, componentId);
             if (child != null) {
                 return child;
             }
         }
         return null;
     }
 
     /**
      * Recurse to all chidlren in component tree and set disabled
      *
      * @param comp     component (inclusive) and all children
      * @param disabled set/unset disabled
      */
     public static void setDisableButtons(Component comp, boolean disabled) {
         // nejprve rekurzivne vsechny deti
         for (Component child : (List<Component>) comp.getChildren()) {
             setDisableButtons(child, disabled);
         }
 
         if (comp instanceof Button) {
             ((Button) comp).setDisabled(disabled);
         }
     }
 
     /**
      * Recurse to all chidlren in component tree and set readonly.
      * Note that there is custom behaviour for some components, please study source code before use.
      *
      * @param comp     component (inclusive) and all children
      * @param readonly set/unset readonly
      */
     public static void setReadonly(Component comp, boolean readonly) {
         final String READONLY_CLASS = " dl-readonly ";
 
         // tyto tridy dale nepropaguji zmeny na sve deti
         List<String> stopPropagationClassNames = Arrays.asList(
                 Bandbox.class.getName(),
                 Combobox.class.getName(),
                 "cz.datalite.zk.components.list.view.DLQuickFilter" // string because of no dependency
         );
 
         // nejprve rekurzivne vsechny deti
         for (Component child : comp.getChildren()) {
             if (!stopPropagationClassNames.contains(child.getClass().getName()))
                 setReadonly(child, readonly);
         }
 
         // pro zname typy komponent nastavim
         if (comp instanceof Combobox) {
             ((Combobox) comp).setReadonly(readonly);
             ((Combobox) comp).setSclass(((Combobox) comp).getSclass() + READONLY_CLASS);
         } else if (comp instanceof Checkbox) {
             ((Checkbox) comp).setDisabled(readonly);
         } else if (comp instanceof Datebox) {
             ((Datebox) comp).setReadonly(readonly);
             ((Datebox) comp).setSclass(((Datebox) comp).getSclass() + READONLY_CLASS);
         } else if (comp instanceof Doublebox) {
             ((Doublebox) comp).setReadonly(readonly);
             ((Doublebox) comp).setSclass(((Doublebox) comp).getSclass() + READONLY_CLASS);
         } else if (comp instanceof Textbox) {
             // obecne kazdy textbox umi readonly
             ((Textbox) comp).setReadonly(readonly);
             ((Textbox) comp).setSclass(((Textbox) comp).getSclass() + READONLY_CLASS);
         } else if (comp instanceof NumberInputElement) {
             // obecne kazdy textbox umi readonly
             ((NumberInputElement) comp).setReadonly(readonly);
             ((NumberInputElement) comp).setSclass(((NumberInputElement) comp).getSclass() + READONLY_CLASS);
         } else if (comp instanceof Button && ((Button) comp).getAttribute("DISABLED_ON_READONLY") != null) {
             ((Button) comp).setDisabled(readonly);
         } else if (comp instanceof Listbox) {
             ((Listbox) comp).setSclass(((Listbox) comp).getSclass() + READONLY_CLASS);
         } else if (comp instanceof Timebox) {
             ((Timebox) comp).setReadonly(readonly);
             ((Timebox) comp).setSclass(((Timebox) comp).getSclass() + READONLY_CLASS);
         }
 
     }
 
     /**
      * Create new window with Executions.createComponents() and opens it in highlighted mode.
      * It registers ZkEvents.ON_REFRESH to master window - refresh after detail calls ZkEvents.ON_REFRESH_PARENT.
      * This method is without detail window parameter.
      *
      * @param master master window on which refresh event is forwarded.
      * @param uri    target URI - @see Executions.createComponents()
      * @return created window
      */
     public static Window openDetailWindow(Component master, String uri) {
         return openDetailWindow(master, uri, new HashMap(), ZkEvents.ON_REFRESH, null);
     }
 
     /**
      * Create new window with Executions.createComponents() and opens it in highlighted mode.
      * It registers ZkEvents.ON_REFRESH to master window - refresh after detail calls ZkEvents.ON_REFRESH_PARENT.
      *
      * @param master     master window on which refresh event is forwarded.
      * @param uri        target URI - @see Executions.createComponents()
      * @param paramName  open window with one parameter, this is the parameter name.
      * @param paramValue open window with one parameter, this is the parameter value.
      * @return created window
      */
     public static Window openDetailWindow(Component master, String uri, String paramName, Object paramValue) {
         return openDetailWindow(master, uri, paramName, paramValue, ZkEvents.ON_REFRESH);
     }
 
     /**
      * Create new window with Executions.createComponents() and opens it in highlighted mode.
      * It registers ZkEvents.ON_REFRESH to master window - refresh after detail calls ZkEvents.ON_REFRESH_PARENT.
      *
      * @param master       master window on which refresh event is forwarded.
      * @param uri          target URI - @see Executions.createComponents()
      * @param paramName    open window with one parameter, this is the parameter name.
      * @param paramValue   open window with one parameter, this is the parameter value.
      * @param refreshEvent custom refresh event name - it must start with on (line onMyEvent)
      * @return created window
      */
     public static Window openDetailWindow(Component master, String uri, String paramName, Object paramValue, String refreshEvent) {
         Map args = new HashMap();
         args.put(paramName, paramValue);
         return openDetailWindow(master, uri, args, refreshEvent, null);
     }
 
     /**
      * Create new window with Executions.createComponents() and opens it in highlighted mode.
      * It calls eventListener on ZkEvents.ON_REFRESH_PARENT.
      *
      * @param master        master window on which refresh event is forwarded.
      * @param uri           target URI - @see Executions.createComponents()
      * @param paramName     open window with one parameter, this is the parameter name.
      * @param paramValue    open window with one parameter, this is the parameter value.
      * @param eventListener call this event on ZkEvents.ON_REFRESH_PARENT in detail window
      * @return created window
      */
     public static Window openDetailWindow(Component master, String uri, String paramName, Object paramValue, EventListener eventListener) {
         Map args = new HashMap();
         args.put(paramName, paramValue);
         return openDetailWindow(master, uri, args, null, eventListener);
     }
 
     /**
      * Create new window with Executions.createComponents() and opens it in highlighted mode.
      * It registers ZkEvents.ON_REFRESH to master window - refresh after detail calls ZkEvents.ON_REFRESH_PARENT.
      *
      * @param master master window on which refresh event is forwarded.
      * @param uri    target URI - @see Executions.createComponents()
      * @param args   open window with parameters - @see Executions.createComponents()
      * @return created window
      */
     public static Window openDetailWindow(Component master, String uri, Map args) {
         return openDetailWindow(master, uri, args, ZkEvents.ON_REFRESH, null);
     }
 
     /**
      * Create new window with Executions.createComponents() and opens it in highlighted mode.
      * It registers ZkEvents.ON_REFRESH to master window - refresh after detail calls ZkEvents.ON_REFRESH_PARENT.
      *
      * @param master       master window on which refresh event is forwarded.
      * @param uri          target URI - @see Executions.createComponents()
      * @param args         open window with parameters - @see Executions.createComponents()
      * @param refreshEvent custom refresh event name - it must start with on (line onMyEvent)
      * @return created window
      */
     public static Window openDetailWindow(Component master, String uri, Map args, String refreshEvent) {
         return openDetailWindow(master, uri, args, refreshEvent, null);
     }
 
     /**
      * flag attribute to detect the pages with already opened the detail
      */
     private static final String DETAIL_WINDOW = "detail.window";
     /**
      * in detail window save actual uri to check
      */
     private static final String DETAIL_WINDOW_URI = "detail.window.uri";
 
     /**
      * Create new window with Executions.createComponents() and opens it in highlighted mode.
      * It registers ZkEvents.ON_REFRESH to master window - refresh after detail calls ZkEvents.ON_REFRESH_PARENT.
      *
      * @param master       master window on which refresh event is forwarded.
      * @param uri          target URI - @see Executions.createComponents()
      * @param args         open window with parameters - @see Executions.createComponents()
      * @param refreshEvent custom refresh event name - it must start with on (line onMyEvent)
      * @return created window
      */
     public static Window openDetailWindow(final Component master, String uri, Map args, String refreshEvent, EventListener eventListener) {
         // if the detail is already opened, prevent the reopening 
         if (master.getAttribute(DETAIL_WINDOW) != null) {
             // get the UUID of the last created detail
             final String uuid = (String) master.getAttribute(DETAIL_WINDOW);
             // try if still exists && contains appropriate attribute (UUID are recycled)
             Component oldDetail = master.getDesktop().getComponentByUuidIfAny(uuid);
             if (oldDetail != null && uri.equals(oldDetail.getAttribute(DETAIL_WINDOW_URI))) {
                 // if so, report and close it
                 LOGGER.warn("Detail window is already opened. Opening of the another one was forbidden.");
                 Messagebox.show(Labels.getLabel("zkcomposer.detail.opened.message"), Labels.getLabel("zkcomposer.detail.opened.title"), Messagebox.OK, Messagebox.INFORMATION);
                return (Window) oldDetail;
             }
         }
 
 
         Component comp = Executions.createComponents(uri, null, args);
         assert comp instanceof Window : "Invalid ZUL file for ZkHelper.openDetail function. ZUL must contain one root componente with type Window.";
 
         Window detail = (Window) comp;
 
         if (refreshEvent != null) {
             detail.addForward(ZkEvents.ON_REFRESH_PARENT, master, refreshEvent);
         }
 
         if (eventListener != null) {
             detail.addEventListener(ZkEvents.ON_REFRESH_PARENT, eventListener);
         }
 
         master.setAttribute(DETAIL_WINDOW, detail.getUuid());
         detail.setAttribute(DETAIL_WINDOW_URI, uri);
 
         detail.doHighlighted();
 
         return detail;
     }
 
     /**
      * Close detail window (with window.detach()).
      *
      * @param window window to close
      */
     public static void closeDetailWindow(Component window) {
         closeDetailWindow(window, false, null);
     }
 
     /**
      * Close detail window (with window.detach()). If refresh parent, new event ZkEvents.ON_REFRESH_PARENT is sent.
      *
      * @param window        window to close
      * @param refreshParent if event to refresh parent should be sent (e.g. true for Ok button, false for cancel button)
      */
     public static void closeDetailWindow(Component window, boolean refreshParent) {
         closeDetailWindow(window, refreshParent, null);
     }
 
     /**
      * Close detail window (with window.detach()). If refresh parent, new event ZkEvents.ON_REFRESH_PARENT is sent (with optional data).
      *
      * @param window        window to close
      * @param refreshParent if event to refresh parent should be sent (e.g. true for Ok button, false for cancel button)
      * @param data          any optional data, it will be available as event.getData() in event handler
      */
     public static void closeDetailWindow(Component window, boolean refreshParent, Object data) {
         window.detach();
 
         if (refreshParent)
             Events.postEvent(new Event(ZkEvents.ON_REFRESH_PARENT, window, data));
     }
 
 
     /**
      * Helper function for convenience - when constructing map - use with pairs of parameter - value.
      *
      * @param params parameters to construct the map
      * @return map with parameters
      */
     public static Map parameterMap(Object... params) {
         assert (params.length % 2 == 0) : "parameterMap has to be called with pairs of parameters.";
 
         Map ret = new HashMap();
 
         for (int i = 1; i < params.length; i = i + 2) {
             ret.put(params[i - 1], params[i]);
         }
 
         return ret;
     }
 }
