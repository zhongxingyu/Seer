 package org.blanco.lacuenta.listeners;
 
 import org.blanco.lacuenta.db.entities.Split;
 import org.blanco.lacuenta.receivers.ResultReceiver;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 /***
  * @author Alexandro Blanco <ti3r.bubblenet@gmail.com>
  * Class that will implements the OnClickListener interface
  * in order to execute the calculus of the bill split and 
  * show the result to the user in the passed ResultReceiver.
  * The class will not format the result.
  */
 public class CalculateClickListener implements OnClickListener{
 
 	private EditText txtTotal = null;
 	private Spinner spnTip = null;
 	private Spinner spnPeople = null;
 	private ResultReceiver resultShower = null;
 	
 	private Split result = null;
 	
 	public CalculateClickListener(EditText txtTotal,
 			Spinner spnTip, Spinner spnPeople, ResultReceiver result){
 		this.txtTotal = txtTotal;
 		this.spnTip = spnTip;
 		this.spnPeople = spnPeople;
 		this.resultShower=result;
 	}
 	
 	public void calculate(){
 		
 		//android.os.Debug.startMethodTracing("calculate.trace");
		double total = Double.valueOf(this.txtTotal.getText().toString());
 	
 		int tip = (this.spnTip.getSelectedItem() != null)? 
 				Integer.valueOf(this.spnTip.getSelectedItem().toString()):0;
 		int people = (this.spnPeople.getSelectedItem() != null)? 
 				Integer.valueOf(this.spnPeople.getSelectedItem().toString()):1;
 		double t = (total * (1 + (0.01*tip)))/people;
 		resultShower.showResult(t);
 		//android.os.Debug.stopMethodTracing();
 		this.result = new Split(total, tip, people, t);
 	}
 	
 	@Override
 	public void onClick(View view) {
 		calculate();
 	}
 
 	public Split getResult() {
 		return result;
 	}
 	
 }
