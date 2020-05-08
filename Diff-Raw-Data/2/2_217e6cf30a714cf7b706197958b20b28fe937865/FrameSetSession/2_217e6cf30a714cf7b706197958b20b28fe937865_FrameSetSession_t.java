package frameset;
 
 import java.awt.event.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.wings.*;
 import org.wings.servlet.*;
 import org.wings.session.*;
 
 public class FrameSetSession
     extends SessionServlet{
 
 
     public FrameSetSession(Session session, HttpServletRequest req) {
         super(session);
     }
 
     public void postInit(ServletConfig config)
         throws ServletException{
 
         /*
         hier startpanel bauen. ob man mit frames arbeitet,
         oder getFrame().getContentPane().add(...) nutzt, ist hier egal.
 
         */
 
         SButton btnShow = new SButton("Show the frames!");
         btnShow.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent event) {
               showFrames();
             }
         });
 
         this.getFrame().getContentPane().add(btnShow);
 
     }
 
     private void showFrames(){
         getSession().setReloadManager(new FrameSetReloadManager());
         SFrameSet newSet = new SFrameSet(new SFrameSetLayout(null, "30,*"));
 
 	// * * * WORKAROUND * * *
 	String server = getFrame().getServerAddress().getAbsoluteAddress();
 	newSet.setServer(server);
 
         this.setFrame(newSet);
 
         SFrame menuFrame = new SFrame("menu frame");
         newSet.add(menuFrame);
         SFrame mainFrame = new SFrame("main frame");
         newSet.add(mainFrame);
 
         menuFrame.getContentPane().setLayout(null);
 
         //mit label geht's
         //menuFrame.getContentPane().add(new SLabel("MenuLabel"));
 
         //mit button nicht
         menuFrame.getContentPane().add(new SButton("MenuButton"));
 
         mainFrame.getContentPane().setLayout(null);
         mainFrame.getContentPane().add(new SLabel("Main"));
     }
 
     public String getServletInfo() {
         return "FrameSet";
     }
 }
