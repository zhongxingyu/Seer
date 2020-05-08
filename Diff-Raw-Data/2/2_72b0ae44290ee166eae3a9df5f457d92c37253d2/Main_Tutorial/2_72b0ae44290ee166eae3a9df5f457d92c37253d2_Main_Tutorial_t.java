 package edu.sru.andgate.bitbot.tutorial;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.interpreter.BotInterpreter;
 import edu.sru.andgate.bitbot.interpreter.InstructionLimitedVirtualMachine;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 
 public class Main_Tutorial extends Activity {
 	
 	private boolean canSimulate = false;
 	private EditText editor; 
 	
 	/*
 	 * Used for sliding the ViewFlipper
 	 */
 	private Animation sIn_left, sOut_left, sIn_right, sOut_right;
 	private TextView botOutput, main_text;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tutorial_main);
 		
 		/*
 		 * recieves content sent from previous view for re-use
 		 */
 		final String tutorialID = getIntent().getExtras().getString("File_ID");
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
 		
 		
 		botOutput = (TextView) findViewById(R.id.ide_std_out);
 		main_text = (TextView) findViewById(R.id.tutorial_text);
 		
 		//get text in the <text> tag of the tutorial chosen(tutorialID)
 		try {
 			main_text.setText(readXML(tutorialID,"text"));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 	
 		/*
 		 * create the text editor and cabinet button
 		 */
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
 		 * sets the view flipper sider animations
 		 */
 		sIn_left = AnimationUtils.loadAnimation(this, R.anim.slidein_left);
 		sOut_left = AnimationUtils.loadAnimation(this, R.anim.slideout_left);
 		sIn_right = AnimationUtils.loadAnimation(this, R.anim.slidein_right);
 		sOut_right = AnimationUtils.loadAnimation(this, R.anim.slideout_right);
 		
 		/*
 		 * Set all the QuickAction buttons & onClick() methods 
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
 				if(tutorialID.equals("getting_started.xml")){
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
 					InterpreteCode();
 					
 					ViewFlipper vf = (ViewFlipper) findViewById(R.id.tutorial_view_flipper);
 					
 					vf.setInAnimation(sIn_right);
 					vf.setOutAnimation(sOut_right);
 					
 					vf.showNext();
 				}else if (canSimulate && simulateFlag == 2){
 					//Graphical Simulation here
 				}else{
 					Toast.makeText(Main_Tutorial.this, "Simulation Available, but answer is not correct", Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 		
 		Button to_code_button = (Button) this.findViewById(R.id.code_btn);
 		to_code_button.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				ViewFlipper vf = (ViewFlipper) findViewById(R.id.tutorial_view_flipper);
 
 				vf.setInAnimation(sIn_right);
 				vf.setOutAnimation(sOut_right);
 				
 				vf.showNext();
 			}
 		});
 		
 		Button back_to_code = (Button) this.findViewById(R.id.ide_back_to_code_btn);
 		back_to_code.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				ViewFlipper vf = (ViewFlipper) findViewById(R.id.tutorial_view_flipper);
 
 				vf.setInAnimation(sIn_left);
 				vf.setOutAnimation(sOut_left);
 				
 				vf.showPrevious();
 			}
 		});
 		
 		/*
 		 * set the sliding drawer open listeners/handlers
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
 				try {
 					TextView help_text = (TextView) findViewById(R.id.help_text);
 					help_text.setText(readXML(tutorialID, "hints"));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 
 		/*
 		 *  set sliding drawer closed listerner/handlers
 		 */
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
 	protected void checkAnswer(File file, String file2) throws IOException 
 	{
 		/*
 		 * Temporary - Need to send file(s) to interpreter and compare abstract 
 		 * 				Syntax Tree's
 		 */
 			String line1 = null;
 			String temp1 = "";
 			String temp2 = "";
 			
 			temp2 = readXML(file2, "answer");
 			
 		  // wrap a BufferedReader around FileReader
 		  BufferedReader bufferedReader1 = new BufferedReader(new FileReader(file.getAbsolutePath()));
 		   // use the readLine method of the BufferedReader to read one line at a time.
 		  // the readLine method returns null when there is nothing else to read.
 		  while ((line1 = bufferedReader1.readLine()) != null)
 		  {
 		    temp1+=line1.toString();
 		  }
 		
 		  // close the BufferedReader(s) when we're done
 		  bufferedReader1.close();
 		
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
 	
 	/*
 	 * Method that recieves an xml file name, and target <tag> 
 	 * 	returns the text in the specified <tag></tag>
 	 */
 	public String readXML(String my_file, String tag_name) throws IOException{
 	 		InputStream is = getAssets().open(my_file);
 			
 	 		try {
 	       		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 	            DocumentBuilder docBuilder;
 				docBuilder = docBuilderFactory.newDocumentBuilder();
 				Document doc = docBuilder.parse(is);
 	            doc.getDocumentElement ().normalize ();
 	            NodeList tutorialText = doc.getElementsByTagName(tag_name);
 	            Element myText = (Element) tutorialText.item(0);
 	            return ((Node)myText.getChildNodes().item(0)).getNodeValue().trim(); 
 	 		} catch (ParserConfigurationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SAXException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			
 			
		    return null;
 		}//end of main
 	 
 	/*
 	 * Method that runs the code through the interpreter
 	 * 	outputs to console view or gives error
 	 */
 	 private void InterpreteCode()
 	    {
 	    	try
 			{
 		    	InstructionLimitedVirtualMachine ilvm = new InstructionLimitedVirtualMachine();
 		    	BotInterpreter bi = new BotInterpreter(null, editor.getText().toString());
 		    	
 		    	ilvm.addInterpreter(bi);
 		    	ilvm.resume(10000);
 		    	
 			}
 	    	catch (Exception e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				botOutput.setText(e.toString());
 			}
 	    	catch (Error e)
 	    	{
 	    		e.printStackTrace();
 	    		botOutput.setText(e.toString());
 	    	}
 	    }	 
 }
