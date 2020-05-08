 package edu.sru.andgate.bitbot.tutorial;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import edu.sru.andgate.bitbot.R;
 
 import android.app.Activity;
 import android.content.Intent;
import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 
 public class Main_Tutorial extends Activity {
 	private boolean canSimulate = false;
 	private EditText editor; 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tutorial_main);
 		
 		final int tutorialID = getIntent().getExtras().getInt("File_ID",0);
 		final int simulateFlag = getIntent().getExtras().getInt("Sim_Flag",0);
 			
 		/*
 		 * Action Items for Sequence, Selection, Iteration buttons
 		 */
 		final ActionItem for_shell = new ActionItem();
 		final ActionItem do_while_shell = new ActionItem();
 		final ActionItem var_decl = new ActionItem();
 		final ActionItem print_shell = new ActionItem();
 		final ActionItem if_shell = new ActionItem();
 		
 		/*
 		 * Action Items for Quick Tools button
 		 */
 		final ActionItem paren_tool = new ActionItem();
 		final ActionItem quote_tool = new ActionItem();
 		final ActionItem brace_tool = new ActionItem();
 		final ActionItem bracket_tool = new ActionItem();
 		
 		/*
 		 * Action Items for Bot Functions button
 		 */
 		final ActionItem move_bot = new ActionItem();
 		final ActionItem rotate_turret = new ActionItem();
 		
 		//create the text editor and cabinet button
 		editor = (EditText) this.findViewById(R.id.editor);
 		editor.setTextSize(12.0f);
 		final SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
 		final Button slideHandleButton = (Button) findViewById(R.id.slideHandleButton);
 		
 		
 		/*
 		 * sets attributes of the action items in the CustomPopUpWindow
 		 */
 		setActionItem(var_decl,editor, "Declare Variable", "Variable Declaration Selected", getResources().getString(R.string.var_declaration));
 		setActionItem(print_shell,editor, "Print to console", "Print Statement Selected", getResources().getString(R.string.print_statement));
 		setActionItem(if_shell,editor, "if statement shell", "if statement Selected", getResources().getString(R.string.if_statement));
 		setActionItem(do_while_shell, editor, "do while shell", "do while statement selected", getResources().getString(R.string.do_while_statement));
 		setActionItem(for_shell,editor, "for statement shell", "for statement selected", getResources().getString(R.string.for_statement));
 		setActionItem(paren_tool,editor, "Parenthesis ( )", "Parenthesis Selected", getResources().getString(R.string.parenthesis));
 		setActionItem(quote_tool,editor, "Quotations \" \"", "Quotes Selected", getResources().getString(R.string.quotations));
 		setActionItem(brace_tool,editor,"Braces { }", "Braces Selected", getResources().getString(R.string.braces));
 		setActionItem(bracket_tool, editor, "Brackets [ ]", "Brackets Selected", getResources().getString(R.string.brackets));
 		
 		
 		/*
 		 * Set all the QuickAction buttons onClick() methods 
 		 */
 		Button sequence_btn = (Button) this.findViewById(R.id.sequence_btn);
 		sequence_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				qa.addActionItem(var_decl);
 				qa.addActionItem(print_shell);
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 								
 		Button selection_btn = (Button) this.findViewById(R.id.selection_btn);
 		selection_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				qa.addActionItem(if_shell);
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 		
 		Button iteration_btn = (Button) this.findViewById(R.id.iteration_btn);
 		iteration_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				qa.addActionItem(for_shell);
 				qa.addActionItem(do_while_shell);
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 		
 		Button tools_btn = (Button) this.findViewById(R.id.tools_btn);
 		tools_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				QuickAction qa = new QuickAction(v);
 				qa.addActionItem(quote_tool);
 				qa.addActionItem(paren_tool);
 				qa.addActionItem(brace_tool);
 				qa.addActionItem(bracket_tool);
 				qa.setAnimStyle(QuickAction.ANIM_AUTO);
 				qa.show();
 			}
 		});
 				
 		Button lock_btn = (Button) this.findViewById(R.id.lock_btn);
 		lock_btn.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				if(tutorialID == R.raw.getting_started){
 					Toast.makeText(Main_Tutorial.this, "Not available in this Tutorial", Toast.LENGTH_SHORT).show();
 				}else{
 					try
 					{
 					    File file = new File(getFilesDir(),"tutorial.txt");
 					    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 					    writer.write(editor.getText().toString());
 					    writer.flush();
 					    writer.close();
 					    checkAnswer(file, tutorialID);
 					} catch (IOException e) 
 					{
 					   e.printStackTrace();
 					}
 				}
 			}
 		});
 		
 		Button simulate_btn = (Button) this.findViewById(R.id.sim_btn);
 		simulate_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				if(simulateFlag == 0){
 					Toast.makeText(Main_Tutorial.this, "No Simulation Available" , Toast.LENGTH_SHORT).show();
 				}else if(canSimulate && simulateFlag == 1){
 					//Console Output here
 					Intent myIntent = new Intent(Main_Tutorial.this, Console.class);
          			startActivity(myIntent);
 				}else if (canSimulate && simulateFlag == 2){
 					//Graphical Simulation here
 				}else{
 					Toast.makeText(Main_Tutorial.this, "Simulation Available, but answer is not correct", Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 		
 		/*
 		 * set the sliding drawer open/closed listeners and handlers
 		 */
 		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() 
 		{
 			@Override
 			public void onDrawerOpened() 
 			{
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					slideHandleButton.setBackgroundResource(R.drawable.vertical_close_arrow);
		        }else{
		        	slideHandleButton.setBackgroundResource(R.drawable.closearrow);
		        }
 				try 
 				{
 		        	TextView generalText = (TextView)findViewById(R.id.tutorial_text);
 					generalText.setText(readTxt(tutorialID));
 				} catch (IOException e) 
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 
 		slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() 
 		{
 			@Override
 			public void onDrawerClosed() 
 			{
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					slideHandleButton.setBackgroundResource(R.drawable.vertical_open_arrow);
		        }else{
		        	slideHandleButton.setBackgroundResource(R.drawable.openarrow);
		        }
 			}
 		});
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.ide_tutorial_menu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case R.id.clear_btn:    editor.setText("");
 	        						break;
 	  
 	    }
 	    return true;
 	}
 	
 	/*
 	 * creates the Action Item with the defined attributes: 
 	 * 		title, message string, text to be added when clicked
 	 */
 	private void setActionItem(ActionItem item, final EditText editor, String title, final String popUpString, final String declaration)
 	{
 		item.setTitle(title);
 		item.setIcon(getResources().getDrawable(R.drawable.icon));
 		item.setOnClickListener(new OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				//Toast.makeText(Main_Tutorial.this, popUpString , Toast.LENGTH_SHORT).show();
 				int start = editor.getSelectionStart();
 				int end = editor.getSelectionEnd();
 				editor.getText().replace(Math.min(start, end), Math.max(start, end),
 				       declaration);
 			}
 		});
 	}
 		
 	/*
 	 * inputs: File, Resource ID of Tutorial File
 	 * Output: User input to file, Toast to let user know if they were correct or not
 	 * Method to check if the user input matches the correct tutorial answer
 	 */
 	protected void checkAnswer(File file, int resId) throws IOException 
 	{
 		/*
 		 * Temporary - Need to send file(s) to interpreter and compare abstract 
 		 * 				Syntax Tree's
 		 */
 			String line1 = null;
 			String line2 = null;
 			String temp1 = "";
 			String temp2 = "";
 			
 		  // wrap a BufferedReader around FileReader
 		  InputStream input = getResources().openRawResource(resId);
 		  BufferedReader bufferedReader1 = new BufferedReader(new FileReader(file.getAbsolutePath()));
 		  InputStreamReader inputreader = new InputStreamReader(input);
 		  BufferedReader bufferedReader2 = new BufferedReader(inputreader);
 
 		  // use the readLine method of the BufferedReader to read one line at a time.
 		  // the readLine method returns null when there is nothing else to read.
 		  while ((line1 = bufferedReader1.readLine()) != null)
 		  {
 		    temp1+=line1.toString();
 		  }
 		  while(!bufferedReader2.readLine().equals("----------")) {}
 		  while((line2 = bufferedReader2.readLine()) != null)
 		  {
 			  temp2+=line2.toString();
 		  }
 		  // close the BufferedReader(s) when we're done
 		  bufferedReader1.close();
 		  bufferedReader2.close();
 		
 		  //Let the user know if they are right or not.
 		  if(temp1.equals(temp2))
 		  {
 			  Toast.makeText(Main_Tutorial.this,"Correct Answer",Toast.LENGTH_SHORT).show();
 			  canSimulate = true;
 			  /*
 			   * call function to simulate code here if not using sim button...
 			   */
 		  }else
 			  {
 				Toast.makeText(Main_Tutorial.this,"Wrong Answer",Toast.LENGTH_SHORT).show();
 				canSimulate = false;
 			  }
 	}
 	
 	//read in a text file
 	 private String readTxt(int id) throws IOException
 	 {
 		 String line = null;
 		 String temp = "";
 		 
 		 InputStream input = getResources().openRawResource(id);
 		 InputStreamReader inputreader = new InputStreamReader(input);
 		  BufferedReader bufferedReader = new BufferedReader(inputreader);
 		  while((line = bufferedReader.readLine()) != null && !line.equals("----------"))
 		  {
 			  temp+=line.toString() + "\n";
 		  }
 		 bufferedReader.close();
 		  
 		 return temp;
 	    }
 	    
 	}
