 package com.example.pokerchance.fragments;
 
 import com.example.pokerchance.R;
 import com.example.pokerchance.model.ChanceModel;
 import com.example.pokerchance.model.ChanceModel.OutsChangeListener;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ChanceFragment extends Fragment {
     
 	private Context mContext;
 	private Button dec;
 	private Button inc;
 	private TextView outs;
 	
 	private TextView ternChance;
 	private TextView riverChance;
 	private TextView trChance;
 	
 	private TextView ternPercent;
 	private TextView riverPercent;
 	private TextView trPercent;
 	
 	final ChanceModel chanceModel = new ChanceModel();
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.tab_chance, container, false);
 		
 		mContext = view.getContext();
 		
 		dec = (Button) view.findViewById(R.id.dec_btn);
 		inc = (Button) view.findViewById(R.id.inc_btn);
 		outs = (TextView) view.findViewById(R.id.outs_tv);
 		
 		ternChance =(TextView) view.findViewById(R.id.chance_tern_tv);
 		riverChance =(TextView) view.findViewById(R.id.chance_river_tv);
 		trChance =(TextView) view.findViewById(R.id.chance_tern_river_tv);
 		
 		ternPercent =(TextView) view.findViewById(R.id.percent_tern_tv);
 		riverPercent =(TextView) view.findViewById(R.id.percent_river_tv);
 		trPercent =(TextView) view.findViewById(R.id.percent_tern_river_tv);
 		
 		
 		dec.setOnClickListener(onDecClick);
 		inc.setOnClickListener(onIncClick);
 		
 		chanceModel.setOutsChangeListener(outsChangeListener);
 		
 		
 		return view;
 	}
 	
 	
 	OnClickListener onDecClick = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			float number = Float.parseFloat(outs.getText().toString());
 			
			if(number > 1){
 				number--;
 				chanceModel.setOuts(number);	
 			}
 			
 		}
 	};
 	
 	OnClickListener onIncClick = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			float number = Float.parseFloat(outs.getText().toString());
 			
			if(number < 52){
 				number++;
 				chanceModel.setOuts(number);
 			}
 			
 		}
 	};
 		
 	OutsChangeListener outsChangeListener = new OutsChangeListener() {
 
 		@Override
 		public void onOutsChange(ChanceModel model) {
 
 			outs.setText(model.getOuts());
 			
 			ternChance.setText(model.getTernChance());
 			riverChance.setText(model.getRiverChance());
 			trChance.setText(model.getTRChance());
 			
 			ternPercent.setText(model.getTernPercent());
 			riverPercent.setText(model.getRiverPercent());
 			trPercent.setText(model.getTRPercent());
 			
 		}
 			
 	
 	};
 
 }
