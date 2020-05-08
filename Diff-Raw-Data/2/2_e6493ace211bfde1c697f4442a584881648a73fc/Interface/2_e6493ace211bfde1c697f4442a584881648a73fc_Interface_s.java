mport java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.JScrollPane;
 
 
 public class Interface extends JFrame implements KeyListener 
 {
     JTextArea systemText = new JTextArea(10,20);
     JTextArea outputText = new JTextArea(10,20);
     JTextArea inputText = new JTextArea(10,20);
     JTextField input = new JTextField(30);
     private int Score = 0;
    
     public Interface()
     {
         super("Interface");
         setSize(900,500);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         JPanel pane = new JPanel();           
         outputText.setBorder(BorderFactory.createMatteBorder(0,0,2,1,Color.GRAY));
         outputText.setBackground(Color.BLACK);
         outputText.setForeground(Color.WHITE); 
         outputText.setFont(new Font("Impact",Font.PLAIN, 12));
         outputText.setLineWrap(true);  
         outputText.setWrapStyleWord(true);
         inputText.setBorder(BorderFactory.createMatteBorder(0,0,0,1,Color.GRAY));
         inputText.setBackground(Color.BLACK);
         inputText.setForeground(Color.WHITE); 
         inputText.setText("> ");
         inputText.setLineWrap(true);  
         inputText.setWrapStyleWord(true);
         inputText.setFont(new Font("Impact",Font.PLAIN, 12));
         input.addKeyListener(this);   
         JScrollPane Scroll = new JScrollPane(outputText);
         Scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         JScrollPane Scrolled = new JScrollPane(inputText);
         Scrolled.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         GridBagLayout gridbag = new GridBagLayout();
         pane.setLayout(gridbag);
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         constraints.weightx = 10;
         constraints.weighty = 100;
         constraints.fill = GridBagConstraints.BOTH;
         constraints.anchor = GridBagConstraints.CENTER;
         gridbag.setConstraints(Scroll, constraints);
         pane.add(Scroll);
         constraints = new GridBagConstraints();
         constraints.gridx = 0;
         constraints.gridy = 1;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         constraints.weightx = 10;
         constraints.weighty = 5;
         constraints.fill = GridBagConstraints.BOTH;
         constraints.anchor = GridBagConstraints.SOUTH;
         gridbag.setConstraints(Scrolled, constraints);        
         pane.add(Scrolled);    
         constraints = new GridBagConstraints();
         constraints.gridx = 0;
         constraints.gridy = 2;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         constraints.weightx = 10;
         constraints.weighty = 1;
         constraints.fill = GridBagConstraints.BOTH;
         constraints.anchor = GridBagConstraints.SOUTH;
         gridbag.setConstraints(input, constraints);
         pane.add(input);
         JPanel rpane = new JPanel();
         systemText.setBackground(Color.BLACK);
         systemText.setForeground(Color.WHITE);
         systemText.setFont(new Font("Impact",Font.PLAIN, 12));
         GridLayout tests = new GridLayout(1,1);
         rpane.setLayout(tests);
         rpane.add(systemText);
         GridLayout grid = new GridLayout(1,2, 0, 0);
         setLayout(grid);
         add(pane);
         add(rpane);
         setVisible(true);
         
         
         startUp();     
     }
     public void startUp()
     {
         outputText.setText("To Start a name game type           : newgame\n"
                           +"To load a saved game type           : loadgame\n"
                           +"To view highscore type                   : highscores\n"
                           +"To view credits type" + "                          " + ": credits\n");
                           
     }
     public void keyReleased(KeyEvent k)
     { 
     }
     public void keyTyped(KeyEvent k)
     { 
     }
     public void keyPressed(KeyEvent k) 
     {
         int keyCode = k.getKeyCode();
          switch (keyCode) {
              case KeyEvent.VK_ENTER:
              String words = input.getText().toLowerCase();
              inputText.setText(inputText.getText() + words + "\n" + "> "); 
              input.setText("");
              try{
                  int Temp = inputText.getLineCount();            
                  int Start = (inputText.getLineStartOffset(Temp-2));            
                  int End = (inputText.getLineEndOffset(Temp-2))-1;
                  inputText.select(Start, End);
                  String Stuff = inputText.getSelectedText();           
                  if(Stuff.equals("Running"))
                  {
                      outputText.setText(outputText.getText() + "Correct \n");                
                  }                
             }
             catch(Exception e)
             {
             }
               break;
             }
         
         /**int keyCode = k.getKeyCode();
          switch (keyCode) {
              case KeyEvent.VK_ENTER:
                 try{
             int Temp = inputText.getLineCount();
             
             int Start = (inputText.getLineStartOffset(Temp-2));
             
             int End = (inputText.getLineEndOffset(Temp-2))-1;
             inputText.select(Start, End);
             String Stuff = inputText.getSelectedText();
             
             if(Stuff.equals("Running"))
             {
                  outputText.setText(outputText.getText() + "Correct \n"); 
                  inputText.setText(inputText.getText() + Stuff + "\n");
                  Temp = inputText.getLineCount();                           
                  End = (inputText.getLineEndOffset(Temp));
                  llowe.select(End, End+1);
                  
             } 
             else
             {
                 llowerText.setText(llowerText.getText()+ "\n");
                 Temp = llowerText.getLineCount();                       
                 End = (llowerText.getLineEndOffset(Temp));
                 llowerText.select(End, End+1);
             }
            }
            catch(Exception e)
            {
            }           
              break;             
         }
         **/
     }
  
     
 }
