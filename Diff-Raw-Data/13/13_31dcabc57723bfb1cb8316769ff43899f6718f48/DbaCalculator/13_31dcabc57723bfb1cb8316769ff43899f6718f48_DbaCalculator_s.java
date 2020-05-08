 package no.ntnu.capgeminitest.dbacalculator;
 
 import no.ntnu.capgeminitest.binding.android.BindingActivity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 public class DbaCalculator extends BindingActivity {
     public static final String TAG = "DbaCalculator";
     public static final String dbaNamespace = "http://org.ntnu.no/groups/capgeminitest/data-binding/";
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate()");
         
//        LayoutInflater.Factory viewFactory = new LayoutInflater.Factory() {
//            @Override public View onCreateView(String name, Context context, AttributeSet attrs) {
//                return calcOnCreateView(name, context, attrs);
//            }
//        };
//        
//        LayoutInflater inflater = (LayoutInflater)this.getSystemService(
//                Context.LAYOUT_INFLATER_SERVICE);
//        inflater = inflater.cloneInContext(this);
//        inflater.setFactory(viewFactory);
//        View layout = inflater.inflate(R.layout.main, null, false);
        setContentViewWithBindings(R.layout.main, null);
     }
     
     public View calcOnCreateView(String name, Context context, AttributeSet attrs) {
         Log.d(TAG, "Creating view with name " + name + " and attributes: ");
         printAttributes(attrs);
         return null;
     }
     
     private void printAttributes(AttributeSet attrs) {
         int count = attrs.getAttributeCount();
         for (int i = 0; i < count; i++) {
             String attrName = attrs.getAttributeName(i);
             String attrValue = attrs.getAttributeValue(i);
             Log.d(TAG, "\t" + attrName + ": " + attrValue);
         }
     }
 }
