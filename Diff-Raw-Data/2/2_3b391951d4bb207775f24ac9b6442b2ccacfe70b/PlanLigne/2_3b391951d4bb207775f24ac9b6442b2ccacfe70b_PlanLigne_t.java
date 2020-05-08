 package fr.lemet;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 
 
 public class PlanLigne extends Activity {
 
     public final static String planLigneBus1 = "com.example.lemet.planLigneBus1";
     private Button passerellePlanLigneBus1 = null;
 
     public final static String planLigneBus2 = "com.example.lemet.planLigneBus2";
     private Button passerellePlanLigneBus2 = null;
 
     public final static String planLigneBus3 = "com.example.lemet.planLigneBus3";
     private Button passerellePlanLigneBus3 = null;
 
     public final static String planLigneBus4 = "com.example.lemet.planLigneBus4";
     private Button passerellePlanLigneBus4 = null;
 
     public final static String planLigneBus5 = "com.example.lemet.planLigneBus5";
     private Button passerellePlanLigneBus5 = null;
 
     public final static String planLigneBus11 = "com.example.lemet.planLigneBus11";
     private Button passerellePlanLigneBus11 = null;
 
     public final static String planLigneBus12 = "com.example.lemet.planLigneBus12";
     private Button passerellePlanLigneBus12 = null;
 
     public final static String planLigneBus13 = "com.example.lemet.planLigneBus13";
     private Button passerellePlanLigneBus13 = null;
 
     public final static String planLigneBus14 = "com.example.lemet.planLigneBus14";
     private Button passerellePlanLigneBus14 = null;
 
     public final static String planLigneBus15 = "com.example.lemet.planLigneBus15";
     private Button passerellePlanLigneBus15 = null;
 
     public final static String planLigneBus16 = "com.example.lemet.planLigneBus16";
     private Button passerellePlanLigneBus16 = null;
 
     public final static String planLigneBus17 = "com.example.lemet.planLigneBus17";
     private Button passerellePlanLigneBus17 = null;
 
     public final static String planLigneA = "com.example.lemet.planLigneA";
     private Button passerellePlanLigneA = null;
 
     public final static String planLigneB = "com.example.lemet.planLigneB";
     private Button passerellePlanLigneB = null;
 
     public final static String planLigneMettis = "com.example.lemet.planLigneMettis";
     private Button passerellePlanLigneMettis = null;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.plan_ligne);
 
         passerellePlanLigneBus1 = (Button) findViewById(R.id.bus_1);
         passerellePlanLigneBus2 = (Button) findViewById(R.id.bus_2);
         passerellePlanLigneBus3 = (Button) findViewById(R.id.bus_3);
         passerellePlanLigneBus4 = (Button) findViewById(R.id.bus_4);
         passerellePlanLigneBus5 = (Button) findViewById(R.id.bus_5);
         passerellePlanLigneBus11 = (Button) findViewById(R.id.bus_11);
         passerellePlanLigneBus12 = (Button) findViewById(R.id.bus_12);
         passerellePlanLigneBus13 = (Button) findViewById(R.id.bus_13);
         passerellePlanLigneBus14 = (Button) findViewById(R.id.bus_14);
         passerellePlanLigneBus15 = (Button) findViewById(R.id.bus_15);
         passerellePlanLigneBus16 = (Button) findViewById(R.id.bus_16);
         passerellePlanLigneBus17 = (Button) findViewById(R.id.bus_17);
         passerellePlanLigneA = (Button) findViewById(R.id.mettis_a);
         passerellePlanLigneB = (Button) findViewById(R.id.mettis_b);
         passerellePlanLigneMettis = (Button) findViewById(R.id.mettis);
 
         passerellePlanLigneBus1.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus1 = new Intent(PlanLigne.this, PlanLigne1.class);
                 activitePlanLigneBus1.putExtra(planLigneBus1, 1);
                 startActivity(activitePlanLigneBus1);
             }
         });
 
         passerellePlanLigneBus2.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus2 = new Intent(PlanLigne.this, PlanLigne2.class);
                 activitePlanLigneBus2.putExtra(planLigneBus2, 2);
                 startActivity(activitePlanLigneBus2);
             }
         });
 
         passerellePlanLigneBus3.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus3 = new Intent(PlanLigne.this, PlanLigne3.class);
                 activitePlanLigneBus3.putExtra(planLigneBus3, 3);
                 startActivity(activitePlanLigneBus3);
             }
         });
 
         passerellePlanLigneBus4.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus4 = new Intent(PlanLigne.this, PlanLigne4.class);
                 activitePlanLigneBus4.putExtra(planLigneBus4, 4);
                 startActivity(activitePlanLigneBus4);
             }
         });
 
         passerellePlanLigneBus5.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus5 = new Intent(PlanLigne.this, PlanLigne5.class);
                 activitePlanLigneBus5.putExtra(planLigneBus5, 5);
                 startActivity(activitePlanLigneBus5);
             }
         });
 
         passerellePlanLigneBus11.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus11 = new Intent(PlanLigne.this, PlanLigne11.class);
                 activitePlanLigneBus11.putExtra(planLigneBus11, 6);
                 startActivity(activitePlanLigneBus11);
             }
         });
 
         passerellePlanLigneBus12.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus12 = new Intent(PlanLigne.this, PlanLigne12.class);
                 activitePlanLigneBus12.putExtra(planLigneBus12, 7);
                 startActivity(activitePlanLigneBus12);
             }
         });
 
         passerellePlanLigneBus13.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus13 = new Intent(PlanLigne.this, PlanLigne13.class);
                 activitePlanLigneBus13.putExtra(planLigneBus13, 8);
                 startActivity(activitePlanLigneBus13);
             }
         });
 
         passerellePlanLigneBus14.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus14 = new Intent(PlanLigne.this, PlanLigne14.class);
                 activitePlanLigneBus14.putExtra(planLigneBus14, 9);
                 startActivity(activitePlanLigneBus14);
             }
         });
 
         passerellePlanLigneBus15.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus15 = new Intent(PlanLigne.this, PlanLigne15.class);
                 activitePlanLigneBus15.putExtra(planLigneBus15, 10);
                 startActivity(activitePlanLigneBus15);
             }
         });
 
         passerellePlanLigneBus16.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus16 = new Intent(PlanLigne.this, PlanLigne16.class);
                 activitePlanLigneBus16.putExtra(planLigneBus16, 11);
                 startActivity(activitePlanLigneBus16);
             }
         });
 
         passerellePlanLigneBus17.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneBus17 = new Intent(PlanLigne.this, PlanLigne17.class);
                 activitePlanLigneBus17.putExtra(planLigneBus17, 12);
                 startActivity(activitePlanLigneBus17);
             }
         });
 
         passerellePlanLigneA.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneA = new Intent(PlanLigne.this, PlanLigneA.class);
                 activitePlanLigneA.putExtra(planLigneA, 13);
                 startActivity(activitePlanLigneA);
             }
         });
 
         passerellePlanLigneB.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent activitePlanLigneB = new Intent(PlanLigne.this, PlanLigneB.class);
                 activitePlanLigneB.putExtra(planLigneB, 14);
                 startActivity(activitePlanLigneB);
             }
         });
 
         passerellePlanLigneMettis.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                Intent activitePlanLigneMettis = new Intent(PlanLigne.this, PlanLigneMettis.class);
                 activitePlanLigneMettis.putExtra(planLigneMettis, 15);
                 startActivity(activitePlanLigneMettis);
             }
         });
     }
 }
