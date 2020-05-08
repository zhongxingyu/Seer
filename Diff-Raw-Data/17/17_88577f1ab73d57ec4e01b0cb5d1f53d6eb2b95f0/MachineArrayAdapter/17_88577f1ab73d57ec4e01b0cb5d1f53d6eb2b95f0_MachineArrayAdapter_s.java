 package net.danopia.mobile.laundryview.data;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import net.danopia.mobile.laundryview.R;
 import net.danopia.mobile.laundryview.structs.Machine;
 
 import java.util.List;
 
 /**
  * Created by danopia on 5/16/13.
  */
 public class MachineArrayAdapter extends ArrayAdapter<Machine> {
     //private static final String tag = "MachineArrayAdapter";
     //private final Context context;
 
     private TextView machineNum;
     private TextView machineStatus;
     private TextView machineMessage;
     private final List<Machine> machines;
 
     public MachineArrayAdapter(Context context, List<Machine> machines)
     {
         super(context, 0, machines);
         //this.context = context;
         this.machines = machines;
         //Collections.sort(buddies);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent)
     {
         View row = convertView;
 
         if (row == null)
         {
             // ROW INFLATION
             LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             row = inflater.inflate(R.layout.machine_list_item, parent, false);
         }
 
         // Get item
         Machine machine = getItem(position);
         //machine.refresh();
 
         machineNum = (TextView) row.findViewById(R.id.machine_number);
         machineStatus = (TextView) row.findViewById(R.id.machine_status);
         machineMessage = (TextView) row.findViewById(R.id.machine_message);
 
         machineNum.setText(machine.number);
 
         TextView bgL = (TextView) row.findViewById(R.id.bg_dark);
         TextView bgR = (TextView) row.findViewById(R.id.bg_light);
 
         bgL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 0));
         bgR.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
 
         switch (machine.status) {
             case 0:
                if (machine.message.startsWith("extended cycle")) {
                     machineStatus.setText("extended cycle");
                     machineMessage.setText(machine.message.substring(14));
                 } else {
                    machineStatus.setText(machine.timeLeft + " minutes left");
                    machineMessage.setText("");
                 }
 
                 bgL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, machine.cycleLength - machine.timeLeft));
                 bgR.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, machine.timeLeft));
 
                 bgL.setBackgroundColor(Color.parseColor("#ff0000"));
                 bgR.setBackgroundColor(Color.parseColor("#ff8080"));
                 break;
 
             case 1:
                if (machine.message.startsWith("cycle ended")) {
                     machineStatus.setText("cycle ended");
                     machineMessage.setText(machine.message.substring(11));
                 } else {
                     machineStatus.setText("");
                    machineMessage.setText("");
                 }
 
                 bgR.setBackgroundColor(Color.parseColor("#80ff80"));
                 break;
 
             case 2:
                 machineStatus.setText("cycle has ended");
                 machineMessage.setText("door still closed");
 
                 bgR.setBackgroundColor(Color.parseColor("#ffff80"));
                 break;
 
             default:
                 machineStatus.setText(machine.message);
                 machineMessage.setText("");
                 bgR.setBackgroundColor(Color.parseColor("#808080"));
         }
 
         machineStatus.setVisibility (( machineStatus.getText() == "") ? TextView.GONE : TextView.VISIBLE);
         machineMessage.setVisibility((machineMessage.getText() == "") ? TextView.GONE : TextView.VISIBLE);
 
         return row;
     }
 }
