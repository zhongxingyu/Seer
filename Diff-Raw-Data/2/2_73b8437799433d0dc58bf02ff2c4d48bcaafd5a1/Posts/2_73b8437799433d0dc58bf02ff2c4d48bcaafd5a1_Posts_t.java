 package com.example.socialcee;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class Posts extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_posts);
         LinearLayout content = new LinearLayout(this);
         content = (LinearLayout) findViewById(R.id.content); 
         LinearLayout ll = CreateBlock(1);
         content.addView(ll);
         ll = CreateBlock(2);
         content.addView(ll);
  
     }
     public LinearLayout CreateBlock(int id)
     {
     	LinearLayout ll = new LinearLayout(this);
     	ll.setOrientation(LinearLayout.VERTICAL);
         LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); 
         layoutParams.setMargins(30, 10, 0, 0);
     	//ll.setId(id);
     	ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
     	layoutParams.setMargins(0, 20, 0, 0);
     	ll.setPadding(40, 50, 40, 0);
     	ll.setId(id+100);
    	ll.addView(AddHeaderBlock()); 
     	ll.addView(AddLine(), new ViewGroup.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT, 2));
     	ll.addView(AddText("The Post!!.....Hello", 20));
     	ll.addView(AddCommentBlock(id, 3),layoutParams);
     	ll.addView(AddEditText(id));
     	ll.addView(AddButton(id));
     	
     	return ll;
     }
     public LinearLayout AddHeaderBlock()
     {
     	LinearLayout ll = new LinearLayout(this);
     	ll.setOrientation(LinearLayout.HORIZONTAL);
     	ll.addView(AddImage(R.drawable.internet_connection, 70, 70, 0));
     	ll.addView(AddImage(R.drawable.arrow, 60, 60, 50));
     	ll.addView(AddImage(R.drawable.internet_connection, 70, 70, 100));
     	
     	return ll;
     }
     public ImageView AddImage(int id, int width, int height, float x)
     {
     	ImageView iv = new ImageView(this);
     	iv.setImageDrawable(getResources().getDrawable(id));
     	iv.setLayoutParams(new LinearLayout.LayoutParams(width, height));
     	//iv.setX(x);
     	
     	return iv;
     }
     public View AddLine()
     {
     	View ruler = new View(this); 
         ruler.setBackgroundColor(Color.GRAY);
         //rl.addView(ruler, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT, 2));
         
         return ruler;
     }
     public TextView AddText(String txt, int size)
     {
     	TextView t = new TextView(this);
     	t.setText(txt);
     	t.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
     	
     	return t;
     }
     public LinearLayout AddCommentBlock(int id, int nc)
     {
     	int i;
     	LinearLayout comments = new LinearLayout(this);
     	comments.setBackgroundColor(Color.LTGRAY);
     	comments.setOrientation(LinearLayout.VERTICAL);
     	
     	comments.setId(id);
     	comments.setTag(id);
     	for(i=1; i<=nc; i++)
     	{
     		comments.addView(AddComment("Comment1"));
     	}
     	
     	return comments;
     }
     public RelativeLayout AddComment(String text)
     {
     	RelativeLayout comment = new RelativeLayout(this);
     	comment.addView(AddText(text,15));
     	
     	return comment;
     }
     public EditText AddEditText(int id)
     {
     	EditText et = new EditText(this);
     	et.setId(id+1000);
         et.setInputType(InputType.TYPE_CLASS_TEXT);
         
         return et;
     }
     public Button AddButton(int id)
     {
     	Button b = new Button(this);
         b.setText("Replay");
         b.setHeight(10);
         b.setWidth(130);
         b.setId(id);
         b.setOnClickListener(getOnClickDoSomething(b));
         
         return b;
     }
     public void EditComments(int id)
     {
     	EditText et = (EditText)findViewById(id+1000);
     	LinearLayout comments = new LinearLayout(this);
     	comments = (LinearLayout) findViewById(id);
     	String text;
 
     	text = et.getText().toString();
     	comments.addView(AddComment(text));
     	et.setText("");
     }
     View.OnClickListener getOnClickDoSomething(final Button b)  {
         return new View.OnClickListener() {
             public void onClick(View v) {
                 EditComments(b.getId());
             }
         };
     }
 }
