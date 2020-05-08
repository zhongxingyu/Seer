 package info.elfapp.lmath;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.text.Html;
 import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
     String dataBuf  = "";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         TextView editText = (TextView) findViewById(R.id.editMsg);
        //editText.setMovementMethod(new ScrollingMovementMethod());
         if(savedInstanceState!=null){
             dataBuf = (String) savedInstanceState.getSerializable("dataBuf");
         }
         editText.setText(Html.fromHtml(dataBuf));
 
     }
 
     @Override
     protected void onSaveInstanceState(Bundle savedInstanceState) {
         savedInstanceState.putSerializable("dataBuf", dataBuf);
         super.onSaveInstanceState(savedInstanceState);
     }
 
     public void showData(View view) {
         EditText matrixFild = (EditText) findViewById(R.id.matrixFild);
         String s = matrixFild.getText().toString();
         TextView editText = (TextView) findViewById(R.id.editMsg);
         //MatrixCoding a = new MatrixCoding("011010101101", 2, 6);
 
         //MatrixCoding a = new MatrixCoding((s.split(" "))[0], Integer.parseInt((s.split(" "))[1]), Integer.parseInt((s.split(" "))[2]));
         MatrixCoding a = new MatrixCoding(s.replaceAll("\n", ""), s.split("\n").length, s.split("\n")[0].length());
         a.doJob();
         String b = "_______________________________ <br>";
         dataBuf = dataBuf + "\n <br>" + b + s.replaceAll("\n", "<br>") + "\n <br> <br>" + a.getFirst() + "\n <br>" + a.getSecond() + "\n <br>" + a.getThird() + "\n <br>" + a.getFourth();
         editText.setText(Html.fromHtml(dataBuf));
     }
 
     public void clearData(View view) {
         TextView editText = (TextView) findViewById(R.id.editMsg);
         editText.setText("");
         dataBuf = "";
     }
 
     public void setOne(View view) {
         EditText matrixFild = (EditText) findViewById(R.id.matrixFild);
         matrixFild.append("1");
     }
 
     public void setNil(View view) {
         EditText matrixFild = (EditText) findViewById(R.id.matrixFild);
         matrixFild.append("0");
     }
     
 }
