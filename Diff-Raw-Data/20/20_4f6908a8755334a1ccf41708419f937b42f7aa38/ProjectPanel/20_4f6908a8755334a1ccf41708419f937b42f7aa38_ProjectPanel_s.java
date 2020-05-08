 package org.powerbat.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.BevelBorder;
 
 import org.powerbat.configuration.Global;
 import org.powerbat.projects.Project;
 
 /**
  * This class is used for the construction of the
  * {@link ProjectSelector}. In regards to other usage, there is none.
  * This will get information from project and use that to render an appropriate
  * UI.
  *
  * @author Naux
  * @see ProjectSelector
  * @since 1.0
  */
 
 public class ProjectPanel extends JPanel implements Comparable<ProjectPanel> {
 
     private static final long serialVersionUID = -3692838815172773196L;
 
     private final Project project;
 
     private static final Font SANS_18 = new Font(Font.SANS_SERIF, Font.BOLD, 18);
     private static final Font SANS_15 = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
     private final JLabel complete;
 
     /**
      * Constructs a new panel dependent on the <tt>project</tt>.
      *
      * @param project The project to get required information such as name and
      *                difficulty.
      * @since 1.0
      */
 
     public ProjectPanel(final Project project) {
         super(new BorderLayout());
 
         final JPanel rightPane = new JPanel(new BorderLayout());
         final JPanel leftPane = new JPanel();
         final JPanel centerPane = new JPanel(new GridLayout(3, 1));
 
         final JLabel difficulty = new JLabel(Project.DIFFICULTY[project.getLevel() - 1]);
         final JLabel name = new JLabel(project.getName());
         final JLabel category = new JLabel(project.getCategory());
 
         final JButton open = new JButton("Open");
 
         complete = new JLabel(project.isComplete() ? new ImageIcon(Global.getImage(Global.COMPLETE_IMAGE)) : null);
 
         this.project = project;
 
         leftPane.setPreferredSize(new Dimension(25, 150));
 
         rightPane.add(open, BorderLayout.SOUTH);
         rightPane.add(complete, BorderLayout.CENTER);
         rightPane.setPreferredSize(new Dimension(75, 150));
 
        setPreferredSize(new Dimension(300, 150));
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
         setBorder(new BevelBorder(BevelBorder.RAISED));
 
         open.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 GUI.openProject(project);
             }
 
         });
 
         complete.setPreferredSize(new Dimension(75, 30));
 
         difficulty.setPreferredSize(new Dimension(200, 30));
         difficulty.setFont(SANS_15);
 
         name.setPreferredSize(new Dimension(200, 50));
         name.setFont(SANS_18);
 
         category.setPreferredSize(new Dimension(200, 30));
         category.setFont(SANS_15);
 
         centerPane.add(name);
         centerPane.add(category);
         centerPane.add(difficulty);
 
         add(leftPane, BorderLayout.WEST);
         add(centerPane, BorderLayout.CENTER);
         add(rightPane, BorderLayout.EAST);
 
     }
 
     /**
      * Used to change the Icon for the completion marker. Only true usage is in
      * the instance construction and during execution.
      *
      * @param isComplete sets the project icon complete
      * @since 1.0
      */
 
     public void setComplete(boolean isComplete) {
         complete.setIcon(isComplete ? new ImageIcon(Global.getImage(Global.COMPLETE_IMAGE)) : null);
     }
 
     /**
      * This will return the project this panel is based off of.
      *
      * @return Project instance used to make this panel.
      * @since 1.0
      */
 
     public Project getProject() {
         return project;
     }
 
     @Override
     public int compareTo(ProjectPanel o) {
         return getProject().getSortName().compareTo(o.getProject().getSortName());
     }
 
 }
