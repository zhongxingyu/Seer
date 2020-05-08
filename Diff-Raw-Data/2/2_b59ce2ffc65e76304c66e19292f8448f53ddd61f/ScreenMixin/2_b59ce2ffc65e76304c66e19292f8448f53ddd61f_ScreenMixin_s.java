 package sofia.app.internal;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.WeakHashMap;
 
 import sofia.app.ActivityStarter;
 import sofia.app.OptionsMenu;
 import sofia.app.Screen;
 import sofia.app.ScreenLayout;
 import sofia.internal.ModalTask;
 import sofia.internal.events.EventDispatcher;
 import sofia.internal.events.OptionalEventDispatcher;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ScrollView;
 
 // -------------------------------------------------------------------------
 /**
  * <p>
  * <em>This class is not intended for public use.</em>
  * </p><p>
  * This class provides the actual implementations of methods in the
  * {@link Screen} class, which "mixes in" this class through composition and
  * delegating methods. These methods are factored out here so that they can be
  * shared between {@link Screen} and its subclasses as well as
  * {@link MapScreen}, which cannot be a subclass of {@link Screen} because it
  * must extend {@code MapActivity} instead.
  * </p>
  *
  * @author  Tony Allevato
  * @version 2012.11.13
  */
 public class ScreenMixin
 {
     //~ Instance/static variables .............................................
 
     public static final String SCREEN_ARGUMENTS =
         "sofia.app.internal.ScreenMixin.arguments";
     public static final String SCREEN_RESULT =
         "sofia.app.internal.ScreenMixin.result";
 
     private static final String INSTANCE_DATA =
             "sofia.app.internal.ScreenMixin.instanceData";
 
     private static HashMap<Long, WeakReference<Object[]>> screenArguments =
         new HashMap<Long, WeakReference<Object[]>>();
 
     private static HashMap<Long, Object> screenResults =
         new HashMap<Long, Object>();
 
     private static HashMap<Long, AbsActivityStarter> startedActivities =
             new HashMap<Long, AbsActivityStarter>();
 
     public static final int ACTIVITY_STARTER_REQUEST_CODE = 0x50F1A001;
 
     private Activity activity;
     private Bundle instanceData;
     private WeakHashMap<LifecycleInjection, Void> injections;
 
     private EventDispatcher initialize = new EventDispatcher("initialize");
 
 
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     /**
      * Initializes a new {@code ScreenInternals} object.
      *
      * @param activity the activity that this object is associated with
      */
     public ScreenMixin(Activity activity)
     {
         this.activity = activity;
         this.instanceData = new Bundle();
         this.injections = new WeakHashMap<LifecycleInjection, Void>();
     }
 
 
     //~ Methods ...............................................................
 
     // ----------------------------------------------------------
     public static ScreenMixin getMixin(Context context)
     {
         try
         {
             Method method = context.getClass().getMethod("getScreenMixin");
             return (ScreenMixin) method.invoke(context);
         }
         catch (Exception e)
         {
             return null;
         }
     }
 
 
     // ----------------------------------------------------------
     public static boolean tryToAddLifecycleInjection(
             Context context, LifecycleInjection injection)
     {
         ScreenMixin mixin = getMixin(context);
 
         if (mixin != null)
         {
             mixin.addLifecycleInjection(injection);
             return true;
         }
         else
         {
             return false;
         }
     }
 
 
     // ----------------------------------------------------------
     public void addLifecycleInjection(LifecycleInjection injection)
     {
         if (!injections.containsKey(injection))
         {
             injections.put(injection, null);
         }
     }
 
 
     // ----------------------------------------------------------
     public void removeLifecycleInjection(LifecycleInjection injection)
     {
            injections.remove(injection);
     }
 
 
     // ----------------------------------------------------------
     public void runPauseInjections()
     {
         for (LifecycleInjection injection : injections.keySet())
         {
             injection.pause();
         }
     }
 
 
     // ----------------------------------------------------------
     public void runResumeInjections()
     {
         for (LifecycleInjection injection : injections.keySet())
         {
             injection.resume();
         }
     }
 
 
     // ----------------------------------------------------------
     public void runDestroyInjections()
     {
         for (LifecycleInjection injection : injections.keySet())
         {
             injection.destroy();
         }
     }
 
 
     // ----------------------------------------------------------
     public Bundle getInstanceData()
     {
         return instanceData;
     }
 
 
     // ----------------------------------------------------------
     public void saveInstanceState(Bundle bundle)
     {
         bundle.putBundle(INSTANCE_DATA, instanceData);
     }
 
 
     // ----------------------------------------------------------
     public void restoreInstanceState(Bundle bundle)
     {
         if (bundle != null)
         {
             instanceData = bundle.getBundle(INSTANCE_DATA);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Displays a confirmation dialog and waits for the user to select an
      * option.
      *
      * @param title the title to display in the dialog
      * @param message the message to display in the dialog
      * @return true if the user clicked the "Yes" option; false if the user
      *     clicked the "No" option or cancelled the dialog (for example, by
      *     pressing the Back button)
      */
     public boolean showConfirmationDialog(
         final String title, final String message)
     {
         ModalTask<Boolean> modal = new ModalTask<Boolean>() {
             @Override
             protected void run()
             {
                 AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 
                 builder.setTitle(title);
                 builder.setMessage(message);
 
                 builder.setPositiveButton("Yes",
                     new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which)
                     {
                         endModal(true);
                     }
                 });
 
                 builder.setNegativeButton("No",
                     new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which)
                     {
                         endModal(false);
                     }
                 });
 
                 builder.setOnCancelListener(
                     new DialogInterface.OnCancelListener() {
                     public void onCancel(DialogInterface dialog)
                     {
                         endModal(false);
                     }
                 });
 
                 builder.show();
             }
         };
 
         return modal.executeTask();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Displays an alert dialog and waits for the user to dismiss it.
      *
      * @param title the title to display in the dialog
      * @param message the message to display in the dialog
      */
     public void showAlertDialog(final String title, final String message)
     {
         ModalTask<Void> modal = new ModalTask<Void>() {
             @Override
             protected void run()
             {
                 AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 
                 builder.setTitle(title);
                 builder.setMessage(message);
 
                 builder.setPositiveButton("OK",
                     new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which)
                     {
                         endModal(null);
                     }
                 });
 
                 builder.setOnCancelListener(
                     new DialogInterface.OnCancelListener() {
                     public void onCancel(DialogInterface dialog)
                     {
                         endModal(null);
                     }
                 });
 
                 builder.show();
             }
         };
 
         modal.executeTask();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Display a popup list to the user and waits for them to select an item
      * from it. Items in the list will be rendered simply by calling the
      * {@link Object#toString()} method. To control the item renderer used to
      * display the list, see
      * {@link #selectItemFromList(String, List, ItemRenderer)}.
      *
      * @param <Item> the type of items in the list, which is inferred from the
      *     {@code list} parameter
      * @param title the title of the popup dialog
      * @param list the list of items to display in the popup
      * @param itemRenderer the item renderer to use to display each item
      * @return the item that was selected from the list, or null if the dialog
      *     was cancelled
      */
     /*public <Item> Item selectItemFromList(
         final String title,
         final List<? extends Item> list,
         final ItemRenderer itemRenderer)
     {
         ModalTask<Item> modal = new ModalTask<Item>() {
             @Override
             protected void run()
             {
                 AlertDialog.Builder builder =
                     new AlertDialog.Builder(activity);
 
                 builder.setTitle(title);
 
                 AnnotatedAdapter adapter =
                     new AnnotatedAdapter(activity, list,
                         ItemRenderer.RenderingContext.DIALOG);
 
                 builder.setAdapter(adapter,
                     new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which)
                     {
                         endModal(list.get(which));
                     }
                 });
 
                 builder.setOnCancelListener(
                     new DialogInterface.OnCancelListener() {
                     public void onCancel(DialogInterface dialog)
                     {
                         endModal(null);
                     }
                 });
 
                 builder.show();
             }
         };
 
         return modal.executeTask();
     }*/
 
 
     // ----------------------------------------------------------
     /**
      * Starts the activity with the specified intent. This method will not
      * return until the new activity is dismissed by the user.
      *
      * @param intent an {@code Intent} that describes the activity to start
      * @param callback the name of the method that should be called on the
      *     previous activity when the new one returns
      */
     public void presentActivity(Intent intent, String callback)
     {
         new ActivityStarter(intent, callback).start(activity);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Starts the activity represented by the specified {@code Screen} subclass
      * and slides it into view. This method will not return until the new
      * screen is dismissed by the user.
      *
      * @param <ResultType>
      * @param screenClass the subclass of {@code Screen} that will be displayed
      * @param resultClass
      * @param args the arguments to be sent to the screen's {@code initialize}
      *     method
      * @return
      */
     public void presentScreen(
             Class<? extends Activity> screenClass, Object... args)
     {
         new ActivityStarter(screenClass, args).start(activity);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Call this method when the current screen is finished and should be
      * closed. The specified value will be passed back to the previous screen
      * and returned from the {@link #presentScreen(Class, Class, Object...)}
      * call that originally presented this screen.
      *
      * @param result the value to pass back to the previous screen
      */
     public void finish(Object result)
     {
         Intent intent = new Intent();
         intent.putExtra(SCREEN_RESULT, registerScreenResult(result));
 
         activity.setResult(Activity.RESULT_OK, intent);
         activity.finish();
     }
 
 
     // ----------------------------------------------------------
     public void startActivityForResult(AbsActivityStarter starter,
             Intent intent, int requestCode)
     {
         long timestamp = System.currentTimeMillis();
         startedActivities.put(timestamp, starter);
         instanceData.putLong("startedActivity", timestamp);
 
         activity.startActivityForResult(intent, requestCode);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Called by a screen to handle an {@code onActivityResult} call that
      * originated from one of the activities above.
      *
      * @param requestCode
      * @param resultCode
      * @param data
      */
     public void handleOnActivityResult(int requestCode, int resultCode,
         Intent data)
     {
         long activityCode = instanceData.getLong("startedActivity", 0);
 
         if (activityCode != 0)
         {
             AbsActivityStarter starter =
                     startedActivities.remove(activityCode);
 
             starter.handleActivityResult(
                     activity, data, requestCode, resultCode);
         }
 
         instanceData.remove("startedActivity");
     }
 
 
     // ----------------------------------------------------------
     public static long registerScreenArguments(Object... args)
     {
         long timestamp = System.currentTimeMillis();
         screenArguments.put(timestamp, new WeakReference<Object[]>(args));
         return timestamp;
     }
 
 
     // ----------------------------------------------------------
     public Object[] getScreenArguments(Intent intent)
     {
         WeakReference<Object[]> ref = null;
 
         if (intent != null)
         {
             long timestamp = intent.getLongExtra(SCREEN_ARGUMENTS, 0);
             ref = screenArguments.get(timestamp);
         }
 
         Object[] args = null;
 
         if (ref != null)
         {
             args = ref.get();
         }
 
         if (args == null)
         {
             args = new Object[0];
         }
 
         return args;
     }
 
 
     // ----------------------------------------------------------
     public static long registerScreenResult(Object result)
     {
         long timestamp = System.currentTimeMillis();
         screenResults.put(timestamp, result);
         return timestamp;
     }
 
 
     // ----------------------------------------------------------
     public static Object takeScreenResult(Intent intent)
     {
         if (intent != null)
         {
             long timestamp = intent.getLongExtra(SCREEN_RESULT, 0);
             return screenResults.remove(timestamp);
         }
         else
         {
             return null;
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Search for and inflate the screen's layout, if possible. Returns true if
      * a layout was found or false if it was not.
      *
      * @return true if a layout was found, otherwise false
      */
     public boolean tryToInflateLayout()
     {
         // Check for a @ScreenLayout annotation on the Screen subclass and
         // inflate the view if so; otherwise, do a smart search for a resource
         // based on the class name. If none is found it is assumed that the
         // user will call setContentView directly in initialize.
 
         ScreenLayout screenLayout =
                 activity.getClass().getAnnotation(ScreenLayout.class);
         int id = ResourceResolver.resolve(activity, screenLayout);
 
         if (id != 0)
         {
            if (screenLayout.scroll())
             {
                 LayoutInflater inflater = activity.getLayoutInflater();
                 ScrollView scroller = new ScrollView(activity);
                 inflater.inflate(id, scroller);
                 activity.setContentView(scroller);
             }
             else
             {
                 activity.setContentView(id);
             }
 
             return true;
         }
         else
         {
             return false;
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Invokes the {@code initialize} method on the activity that matches the
      * specified argument list, if there is one.
      *
      * @param args the arguments to pass to {@code initialize}
      */
     public void invokeInitialize(Object[] args)
     {
         // Load any persistent data saved from a previous instance of the
         // activity.
         PersistenceManager.getInstance().loadPersistentContext(activity);
 
         // Dynamically call the initialize method that takes the specified
         // arguments, or throw an exception if none exists.
         if (initialize.isSupportedBy(activity, args))
         {
             initialize.dispatch(activity, args);
         }
         else
         {
             StringBuffer argClasses = new StringBuffer();
             for (int i = 0; i < args.length; i++)
             {
                 argClasses.append(args[i].getClass().getSimpleName());
 
                 if (i < args.length - 1)
                 {
                     argClasses.append(", ");
                 }
             }
 
             throw new IllegalStateException(
                     activity.getClass().getSimpleName() + " does not have a "
                     + "method initialize(" + argClasses.toString() + ") or "
                     + "one with compatible arguments.");
         }
     }
 
 
     // ----------------------------------------------------------
     public boolean onCreateOptionsMenu(Menu menu)
     {
         OptionsMenu menuAnnotation =
                 activity.getClass().getAnnotation(OptionsMenu.class);
 
         if (menuAnnotation != null)
         {
             int id = ResourceResolver.resolve(activity, menuAnnotation);
 
             MenuInflater inflater = activity.getMenuInflater();
             inflater.inflate(id, menu);
 
             return true;
         }
         else
         {
             return false;
         }
     }
 
 
     // ----------------------------------------------------------
     public boolean onMenuItemSelected(int featureId, MenuItem item)
     {
         String id = activity.getResources().getResourceEntryName(
                 item.getItemId());
 
         boolean called = false;
 
         if (id != null)
         {
             OptionalEventDispatcher event =
                     new OptionalEventDispatcher(id + "Clicked");
             called = event.dispatch(activity, item);
         }
 
         return called;
     }
 }
