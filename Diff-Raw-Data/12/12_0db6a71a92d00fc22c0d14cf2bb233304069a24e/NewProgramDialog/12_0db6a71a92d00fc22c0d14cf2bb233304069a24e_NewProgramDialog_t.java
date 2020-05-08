 //----------------------------------------------------------------------------
 // $Id$
 //----------------------------------------------------------------------------
 
 package hexgui.gui;
 
 import hexgui.util.SpringUtilities;
 import hexgui.gui.ShowError;
 import hexgui.gui.Program;
 
 import java.util.Vector;
 import javax.swing.*;
 import javax.swing.text.html.HTMLEditorKit;
 import java.awt.*;
 import java.awt.event.*;
 
 /** Dialog for adding a new program. */
 public final class NewProgramDialog
     extends JDialog implements ActionListener
 {
 
     public NewProgramDialog(Frame owner)
     {
         super(owner, true);
         setTitle("Add New Program");
         init(null);
     }
 
     public NewProgramDialog(Frame owner, Program program)
     {
         super(owner, true);
         setTitle("Edit Program");
         init(program);
     }
 
     private void init(Program program)
     {
         JEditorPane info = new JEditorPane();
         info.setEditable(false);
         info.setEditorKit(new HTMLEditorKit());
         
         if (program != null) {
             info.setText("Edit the program's fields. Changing the program's " +
                          "will actully add a new program by that name.");
         } else {
             info.setText("<h3>Enter command for new Hex program</h3>"+
                          "<p>The command can be simply the name of the " +
                          "executable file, or the name plus any options " +
                          "you wish to set.  The working directory can be left " +
                          "blank if the program does not need a special " +
                          "working directory. Enter a simple descriptive name " +
                          "to refer to this program.");
         }
         add(info, BorderLayout.NORTH);
         add(createProgramPanel(program), BorderLayout.CENTER);
         add(createButtonPanel(), BorderLayout.SOUTH);
 
         if (program != null) {
             setPreferredSize(new Dimension(500, 180));
         } else {
            setPreferredSize(new Dimension(500, 280));
         }
         pack();
 
         setResizable(false);
 
         setVisible(true);
     }
 
     public void actionPerformed(ActionEvent e)
     {
         String cmd = e.getActionCommand();
         if (cmd.equals("OK")) {
 
             String name = m_name.getText();
             String command = m_command.getText();
             Program newprog = new Program(name, command, m_working.getText());
 
             // add the program to the list of programs
             Vector<Program> programs = Program.load();
 
             boolean found = false;
             for (int i=0; i<programs.size(); ++i) {
                 Program prog = programs.get(i);
 
                 // this is the one we were editing
                 // FIXME: editing a program and giving it a new name will 
                 // cause a new program to be added. 
                 if (prog.m_name.equals(newprog.m_name)) {
                     found = true;
                     prog.m_command = newprog.m_command;
                     prog.m_working = newprog.m_working;
                     break;
                 }
             }
             if (!found)
                 programs.add(newprog);
 
             Program.save(programs);
             
             dispose();
 
         } else if (cmd.equals("Cancel")) {
             dispose();
         }
     }
 
     private JPanel createProgramPanel(Program program)
     {
         JPanel panel = new JPanel(new SpringLayout());
         JLabel l;
 
         l = new JLabel("Name:", JLabel.TRAILING);
         panel.add(l);
 
         m_name = new JTextField(40);
         if (program != null) m_name.setText(program.m_name);
         l.setLabelFor(m_name);
         panel.add(m_name);
 
         l = new JLabel("Command:", JLabel.TRAILING);
         panel.add(l);
         m_command = new JTextField(40);
         if (program != null) m_command.setText(program.m_command);
         l.setLabelFor(m_command);
         panel.add(m_command);
 
         l = new JLabel("Working Directory:", JLabel.TRAILING);
         panel.add(l);
         m_working = new JTextField(40);
         if (program != null) m_working.setText(program.m_working);
         l.setLabelFor(m_working);
         panel.add(m_working);
 
         SpringUtilities.makeCompactGrid(panel,
                                         3, 2,        // rows, cols
                                         6, 6,        // initX, initY
                                         6, 6);       // xPad, yPad
 
         return panel;
     }
 
     private JPanel createButtonPanel()
     {
         JPanel panel = new JPanel();
         
         JButton button = new JButton("  OK  ");
         button.addActionListener(this);
         button.setActionCommand("OK");
         panel.add(button);
         
         button = new JButton("Cancel");
         button.addActionListener(this);
         button.setActionCommand("Cancel");
         panel.add(button);
 
         return panel;
     }
 
     JTextField m_name;
     JTextField m_command;
     JTextField m_working;
 
     Program m_program;
 }
 
 //----------------------------------------------------------------------------
