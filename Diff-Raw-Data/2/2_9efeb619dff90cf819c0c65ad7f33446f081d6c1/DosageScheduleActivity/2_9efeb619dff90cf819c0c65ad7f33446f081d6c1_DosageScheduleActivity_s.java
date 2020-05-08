 package com.stehno.pillcounter;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import java.text.DecimalFormat;
 
 /**
  * @author cjstehno
  */
 public class DosageScheduleActivity extends Activity {
 
     private static final String UTF_8 = "utf-8";
     private static final String TEXT_HTML = "text/html";
     private static final DecimalFormat decFormat = new DecimalFormat("0.00");
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         setContentView(R.layout.schedule);
 
         final DosageDto dto = (DosageDto)getIntent().getExtras().getSerializable(DosageDto.KEY);
 
         final TableLayout tableLayout = (TableLayout)findViewById(R.id.schedule_table);
 
         int day = 1;
         for( DailyDosage dd: dto.dailyDosages()){
             final TableRow tableRow = new TableRow(this);
 
             tableRow.addView( createCell(String.valueOf(day++)) );
             tableRow.addView( createCell(decFormat.format(dd.getMg())) );
             tableRow.addView( createCell(decFormat.format(dd.getPills())) );
 
             tableLayout.addView( tableRow );
         }
 
         final TableRow totalRow = new TableRow(this);
         totalRow.addView( createCell("Total pills:") );
         totalRow.addView( createCell(decFormat.format(dto.getTotalPills())) );
 
         tableLayout.addView(totalRow);
     }
 
     @Override
     public boolean onCreateOptionsMenu( final Menu menu ) {
         super.onCreateOptionsMenu( menu );
         getMenuInflater().inflate( R.menu.options_menu, menu );
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected( final MenuItem item ) {
         switch (item.getItemId()){
             case R.id.share:
                 Intent i = new Intent(Intent.ACTION_SEND);
                 i.setType("text/plain");
                 i.putExtra(Intent.EXTRA_SUBJECT, "Dosage Schedule");
                 i.putExtra(Intent.EXTRA_TEXT, renderTextTable((DosageDto)getIntent().getExtras().getSerializable(DosageDto.KEY)) );
                 startActivity(Intent.createChooser(i, "Share with..."));
 
                 return true;
         }
         return false;
     }
 
     private String renderTextTable( DosageDto dto ){
         StringBuilder str = new StringBuilder();
 
         str.append("Day\tmg/day\tpills/day\n");
 
         int day = 1;
         for( DailyDosage dd: dto.dailyDosages()){
             final String mg = decFormat.format(dd.getMg());
             final String pills = decFormat.format(dd.getPills());
             str.append(String.valueOf(day++)).append('\t').append(mg).append('\t').append(pills).append('\n');
         }
 
         str.append("Total pills: " + decFormat.format(dto.getTotalPills()));
 
         return str.toString();
     }
 
     private TextView createCell( String value ){
         final TextView cell = new TextView(this);
        cell.setTextColor(Color.BLACK);
         cell.setPadding(3,3,3,3);
         cell.setText(value);
         return cell;
     }
 }
