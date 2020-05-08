 package com.ruoyiwang.chi.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.ruoyiwang.chi.R;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * This view is kind of a flowLayout, a tag/label wall
  * @author esong
  *
  */
 public class ChiTagView extends LinearLayout {
 	private int iParentWidth;
 	private int curWidth;
 	private LinearLayout curLine;
 	private static LayoutInflater lif;
 	private static List<LinearLayout> lines = new ArrayList<LinearLayout>();
 	
 	public ChiTagView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		iParentWidth = context.getResources().getDisplayMetrics().widthPixels;
 		
 		lif = LayoutInflater.from(context);
 		
 		createNewLine();
 	}
 	
 	public static TextView createChiTag(String tagName, int tagCount){
 		String typeText = tagName + " (" + tagCount + ")";
 		TextView tagView = (TextView) lif.inflate(R.layout.chi_tag, null);
 		
 		tagView.setText(typeText);
 		tagView.setTag(tagName);
 		tagView.setBackgroundResource(R.drawable.chi_tag_shape_stateful);
 
 		tagView.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				if(v.isSelected()){
 					v.setSelected(false);
 				}
 				else{
 					v.setSelected(true);
 				}
 			}
 		});
 		
 		return tagView;
 	}
 	
 	public void setTag(View tag){
 		tag.measure(0, 0);
 		curWidth += tag.getMeasuredWidth();
 		
 		if(curWidth > iParentWidth - 20){
 			flush();	// flush all tags 
 			createNewLine();
			curWidth += tag.getMeasuredWidth(); // curWidth was setted to 0 in createNewLine
 		}
 		
 		curLine.addView(tag);
 	}
 	
 	public void flush(){
 		lines.add(curLine);
 		this.addView(curLine);
 	}
 	
 	private void createNewLine(){
 		curLine = (LinearLayout) lif.inflate(R.layout.chi_tag_layout, null);
 		curWidth = 0;
 	}
 	
 	public List<String> getAllSelectedTags(){
 		List<String> ret = new ArrayList<String>();
 		for (LinearLayout tags : lines){
 			for(int i = 0 ; i < tags.getChildCount();i++){
 				View tag = tags.getChildAt(i);
 				if(tag.isSelected())
 					ret.add((String)tag.getTag());
 			}
 		}
 		return ret;
 	}
 
 }
