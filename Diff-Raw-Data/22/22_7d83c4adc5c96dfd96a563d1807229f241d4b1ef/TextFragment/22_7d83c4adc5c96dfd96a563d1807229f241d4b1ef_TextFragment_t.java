 package hk.hku.cs.srli.monkeydemo.demo;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import hk.hku.cs.srli.monkeydemo.R;
import hk.hku.cs.srli.widget.HoverTextView;
 import hk.hku.cs.srli.widget.TooltipManager;
 
 public class TextFragment extends DemoFragmentBase {
     
    HoverTextView textView;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View rootView = super.onCreateView(inflater, container, savedInstanceState);
         
        textView = (HoverTextView) rootView.findViewById(R.id.textView1);
      
 //        rootView.findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
 //            
 //            @Override
 //            public void onClick(View v) {
 //                showTooltip();
 //            }
 //        });
         
         return rootView;
     }
     
     @Override
     protected int getLayoutResource() {
         return R.layout.fragment_text;
     }
     
     public void showTooltip() {
         TooltipManager.showAndHide(textView, textView.getText(), TooltipManager.LONG_DELAY);
     }
 }
