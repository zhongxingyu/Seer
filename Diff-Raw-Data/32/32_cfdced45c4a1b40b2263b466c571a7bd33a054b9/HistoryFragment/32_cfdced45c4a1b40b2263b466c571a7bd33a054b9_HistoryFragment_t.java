 package com.innutrac.poly.innutrac;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.echo.holographlibrary.Line;
 import com.echo.holographlibrary.LineGraph;
 import com.echo.holographlibrary.LinePoint;
 import com.innutrac.poly.innutrac.database.FoodDatabase;
 
 import android.app.Fragment;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class HistoryFragment extends Fragment {
 
 	Spinner spinner;
 	FoodDatabase totaledfdb;
 	ArrayList<DailyPlan> intakeHistory;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.fragment_history, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		totaledfdb = new FoodDatabase(getActivity());
 		totaledfdb.open("UserTotalyDailyIntake");
 		intakeHistory = totaledfdb.getAllDailyIntakes();
 
 		DailyPlan todayDP = ((MainActivity) getActivity()).getTodayDailyPlan();
 		intakeHistory.add(todayDP);
 		Collections.reverse(intakeHistory);
 
 		initHistory();
 		initGraph();
 	}
 
 	public void initGraph() {
 		int limit = 0;
 
 		LineGraph lg = (LineGraph) getView().findViewById(R.id.linegraph);
 
 		Line l = new Line();
 		Line limit_line = new Line();
 		LinePoint p = new LinePoint();
 
 		// must draw at x = 0 and x = 10 for two different points when there are
 		// > 1 point
 		// divide max calories by 500 to scale to graph
 
 		// draw the limit line
 		limit_line.setShowingPoints(false);
 		p.setX(0);
 		p.setY(4);
 		limit_line.addPoint(p);
 		p = new LinePoint();
 		p.setX(10);
 		p.setY(4);
 		limit_line.addPoint(p);
 
 		if (intakeHistory.size() > 7) {
 			limit = 7;
 		} else {
 			limit = intakeHistory.size() - 1;
 		}
 
 		// if (limit == 0) {
 		float counter = 0;
		for (int i = limit; i >= 0; i--) {
 			p = new LinePoint();
 			p.setX(counter);
 			counter += 1.67;
 			p.setY((float) (intakeHistory.get(i).getCurrentCalories() / 500));
 			l.addPoint(p);
 		}
 		// }
//		 p = new LinePoint();
//		 p.setX(10);
//		 p.setY(6);
//		 l.addPoint(p);
 		// l.setColor(Color.parseColor("#33CC33"));
 		// p=new LinePoint();
 		// p.setX(0);
 		// p.setY(7);
 		// l.addPoint(p);
 		// p=new LinePoint();
 		// p.setX(300);
 		// p.setY(9);
 		// l.addPoint(p);
 		// l.setColor(Color.parseColor("#00FFFF"));
		
		if (limit >= 0) {
			l.setColor(Color.parseColor("#33CC33"));
 			lg.addLine(l);
 		}
 		limit_line.setColor(Color.parseColor("#FFBB33"));
 		lg.addLine(limit_line);
 		lg.setRangeY(0, 10);
 		lg.setLineToFill(0);
 	}
 
 	private void initHistory() {
 		spinner = (Spinner) getActivity().findViewById(R.id.history_spinner);
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int position, long id) {
 				fillHistoryPanel(position);
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 
 			}
 		});
 		List<String> spinnerList = new ArrayList<String>();
 
 		for (int i = 0; i < intakeHistory.size(); i++) {
 			if (i == 0) {
 				spinnerList.add("Today");
 			} else {
 				DailyPlan tempDp = intakeHistory.get(i);
 				spinnerList.add(tempDp.getFormattedPlanLogTime());
 			}
 		}
 
 		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
 				getActivity(), android.R.layout.simple_spinner_item,
 				spinnerList);
 		dataAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(dataAdapter);
 
 	}
 
 	private void fillHistoryPanel(int i) {
 		DailyPlan dp = intakeHistory.get(i);
 		DailyPlan todayDP = ((MainActivity) getActivity()).getTodayDailyPlan();
 
 		DecimalFormat df = new DecimalFormat("0.00");
 
 		TextView mTextView = (TextView) getActivity().findViewById(
 				R.id.calories_current);
 		mTextView.setText(df.format(dp.getCurrentCalories()));
 		mTextView = (TextView) getActivity().findViewById(R.id.calories_total);
 		mTextView.setText(df.format(todayDP.getTotalCalories()));
 
 		// set carbohydrates
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_carb_ratio);
 		mTextView.setText(df.format(dp.getCurrentCarbohydrate()) + " / "
 				+ df.format(todayDP.getTotalCarbohydrate()));
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_carb_percent);
 		mTextView.setText(df.format(getPercent(dp.getCurrentCarbohydrate(),
 				todayDP.getTotalCarbohydrate())) + "%");
 
 		// set protein
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_protein_ratio);
 		mTextView.setText(df.format(dp.getCurrentProtein()) + " / "
 				+ df.format(todayDP.getTotalProtein()));
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_protein_percent);
 		mTextView.setText(df.format(getPercent(dp.getCurrentProtein(),
 				todayDP.getTotalProtein()))
 				+ "%");
 
 		// set sodium
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_sodium_ratio);
 		mTextView.setText(df.format(dp.getCurrentSodium()) + " / "
 				+ df.format(todayDP.getTotalSodium()));
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_sodium_percent);
 		mTextView.setText(df.format(getPercent(dp.getCurrentSodium(),
 				todayDP.getTotalSodium()))
 				+ "%");
 
 		// set cholesterol
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_chole_ratio);
 		mTextView.setText(df.format(dp.getCurrentCholesterol()) + " / "
 				+ df.format(todayDP.getTotalCholesterol()));
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_chole_percent);
 		mTextView.setText(df.format(getPercent(dp.getCurrentCholesterol(),
 				todayDP.getTotalCholesterol())) + "%");
 
 		// set unsaturated fat
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_unsat_fat_ratio);
 		mTextView.setText(df.format(dp.getCurrentUnsatFats()) + " / "
 				+ df.format(todayDP.getTotalUnsatFats()));
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_unsat_fat_percent);
 		mTextView.setText(df.format(getPercent(dp.getCurrentUnsatFats(),
 				todayDP.getTotalUnsatFats())) + "%");
 
 		// set saturated fat
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_sat_fat_ratio);
 		mTextView.setText(df.format(dp.getCurrentSatFats()) + " / "
 				+ df.format(todayDP.getTotalSatFats()));
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_sat_fat_percent);
 		mTextView.setText(df.format(getPercent(dp.getCurrentSatFats(),
 				todayDP.getTotalSatFats()))
 				+ "%");
 
 		// set sugar
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_sugar_ratio);
 		mTextView.setText(df.format(dp.getCurrentSugar()) + " / "
 				+ df.format(todayDP.getTotalSugar()));
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_sugar_percent);
 		mTextView.setText(df.format(getPercent(dp.getCurrentSugar(),
 				todayDP.getTotalSugar()))
 				+ "%");
 
 		// set fiber
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_fiber_ratio);
 		mTextView.setText(df.format(dp.getCurrentFiber()) + " / "
 				+ df.format(todayDP.getTotalFiber()));
 		mTextView = (TextView) getActivity().findViewById(
 				R.id.history_fiber_percent);
 		mTextView.setText(df.format(getPercent(dp.getCurrentFiber(),
 				todayDP.getTotalFiber()))
 				+ "%");
 	}
 
 	private double getPercent(double nom, double denom) {
 		if (denom == 0)
 			return 0;
 		System.out.println("nom " + nom);
 		System.out.println("denom " + denom);
 		double ans = (nom * 100f) / denom;
 		DecimalFormat df = new DecimalFormat("#.#");
 		return Double.parseDouble(df.format(ans));
 	}
 
 }
