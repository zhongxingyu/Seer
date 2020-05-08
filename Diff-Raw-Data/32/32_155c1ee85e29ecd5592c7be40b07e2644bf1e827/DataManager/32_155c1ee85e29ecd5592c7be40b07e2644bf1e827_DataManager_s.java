 package uk.org.smithfamily.mslogger;
 
import java.util.*;
 
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import uk.org.smithfamily.mslogger.ecuDef.OutputChannel;
import android.content.*;
 
 public class DataManager extends Observable
 {
     private final Map<String, OutputChannel> dataChannels = new HashMap<String, OutputChannel>();
     private static DataManager instance = new DataManager();
     private boolean disabled = true;
 
     private DataManager()
     {
         final IntentFilter dataFilter = new IntentFilter(Megasquirt.NEW_DATA);
         final IntentFilter connectionFilter = new IntentFilter(Megasquirt.CONNECTED);
         final IntentFilter disconnectionFilter = new IntentFilter(Megasquirt.DISCONNECTED);
         ApplicationSettings.INSTANCE.getContext().registerReceiver(mReceiver, dataFilter);
         ApplicationSettings.INSTANCE.getContext().registerReceiver(mReceiver, connectionFilter);
         ApplicationSettings.INSTANCE.getContext().registerReceiver(mReceiver, disconnectionFilter);
     }
 
     public static DataManager getInstance()
     {
         return instance;
     }
 
     public void tickle()
     {
         setChanged();
         notifyObservers();
     }
 
     private final BroadcastReceiver mReceiver = new BroadcastReceiver()
     {
         @Override
         public void onReceive(final Context context, final Intent intent)
         {
             final String action = intent.getAction();
             if (action.equals(Megasquirt.NEW_DATA))
             {
                 disabled = false;
             }
             else if (action.equals(Megasquirt.CONNECTED))
             {
                 disabled = false;
             }
             else if (action.equals(Megasquirt.DISCONNECTED))
             {
                 disabled = true;
             }
             tickle();
         }
     };
 
     public Map<String, OutputChannel> getOutputChannels()
     {
         return dataChannels;
     }
 
     public void addOutputChannel(final OutputChannel o)
     {
         dataChannels.put(o.getName(), o);
     }
 
     public double getField(final String channelName)
     {
 
         double value = 0;
         final OutputChannel oc = dataChannels.get(channelName);
         if (oc == null)
         {
             return value;
         }
         value = oc.getValue();
         return value;
     }
 
     public boolean isDisabled()
     {
         return disabled;
     }
 
 }
