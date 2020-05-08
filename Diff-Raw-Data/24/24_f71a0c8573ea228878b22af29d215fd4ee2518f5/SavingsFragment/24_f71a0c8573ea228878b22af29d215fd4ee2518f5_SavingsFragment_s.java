 package com.avantics.savingscalc.common.fragments;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.avantics.savingscalc.common.R;
 
 public class SavingsFragment extends BindableFragment {
     LinearLayout oneMonthContainer = null, oneYearContainer = null, fourYearsContainer = null, percentageContainer = null;
     TextView oneMonthValue = null, oneYearValue = null, fourYearsValue = null, percentageValue = null;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         View view = inflater.inflate(R.layout.quote_savings, container, false);
 
         bindView(view);
 
         oneMonthContainer = (LinearLayout) view.findViewById(R.id.oneMonthSavingContainer);
         oneMonthValue = (TextView) view.findViewById(R.id.Savings1Month);
 
         oneYearContainer = (LinearLayout) view.findViewById(R.id.oneYearSavingContainer);
         oneYearValue = (TextView) view.findViewById(R.id.Savings1Year);
 
         fourYearsContainer = (LinearLayout) view.findViewById(R.id.fourYearSavingContainer);
         fourYearsValue = (TextView) view.findViewById(R.id.Savings4Years);
 
         percentageContainer = (LinearLayout) view.findViewById(R.id.savingPercentageContainer);
         percentageValue = (TextView) view.findViewById(R.id.savingsPercentage);
 
         oneMonthContainer.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 oneMonthValue.setTextAppearance(getActivity().getApplicationContext(), R.style.highlightedSavingLabel);
                 oneYearValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
                 fourYearsValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
                 percentageValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
             }
         });
 
         oneYearContainer.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 oneMonthValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
                 oneYearValue.setTextAppearance(getActivity().getApplicationContext(), R.style.highlightedSavingLabel);
                 fourYearsValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
                 percentageValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
             }
         });
 
         fourYearsContainer.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 oneMonthValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
                 oneYearValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
                 fourYearsValue.setTextAppearance(getActivity().getApplicationContext(), R.style.highlightedSavingLabel);
                 percentageValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
             }
         });
 
         percentageContainer.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 oneMonthValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
                 oneYearValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
                 fourYearsValue.setTextAppearance(getActivity().getApplicationContext(), R.style.centerAlignLabel);
                 percentageValue.setTextAppearance(getActivity().getApplicationContext(), R.style.highlightedSavingLabel);
             }
         });
 
         return view;
     }
 }
