 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GraphDB;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 /**
  *
  * @author michal
  */
 public class Grapher extends JPanel implements ActionListener {
 
     // user wxs001 password 6cyX6zM9
     Client gClient;
     Map textFieldMap;
     JPanel graphPanel;
     JPanel buttonPanel;
     JFrame frame;
     boolean authenticated = false;
     ArrayList<Point> regPoints;
 
     public static void main(String[] args) {
         Grapher g = new Grapher("localhost", 2000);
     }
 
     public Grapher(String host, int port) {
         initWindow();
         gClient = new Client(host, port);
     }
 
     public void initWindow() {
         graphPanel = new JPanel();
 
         makeButtonPanel();
 
 
         frame = new JFrame("RegGraph");
         frame.setSize(800, 600);
         frame.setResizable(false);
 ////        frame.addWindowListener(new WindowAdapter() {
 //
 ////            @Override
 ////            public void windowClosing(WindowEvent e) {
 ////                System.out.println("windowclosing.");
 ////                if (gClient.connected) {
 ////                    gClient.disconnect();
 ////                    System.exit(0);
 ////                }
 ////            }
 ////        });
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.add(this);
         this.add(buttonPanel);
 
         frame.setVisible(true);
     }
 
     private void makeButtonPanel() {
         buttonPanel = new JPanel();
 
         JLabel userL = new JLabel("User:");
         JLabel passL = new JLabel("Password:");
         JLabel modL = new JLabel("Module ID");
 
         JTextField modID = new JTextField(3);
         JTextField user = new JTextField(6);
         JTextField password = new JPasswordField(10);
 
         JButton login = new JButton("Login");
         JButton submitQuery = new JButton("Get Data");
         JButton disconnect = new JButton("Disconnect");
 
         login.addActionListener(this);
         disconnect.addActionListener(this);
         submitQuery.addActionListener(this);
 
         // Puts the text fields into a hashmap so that we can access them
         // later.
         textFieldMap = new HashMap<String, JTextField>();
         textFieldMap.put("user", user);
         textFieldMap.put("pass", password);
         textFieldMap.put("module", modID);
 
         buttonPanel.add(userL);
         buttonPanel.add(user);
         buttonPanel.add(passL);
         buttonPanel.add(password);
         buttonPanel.add(login);
         buttonPanel.add(modL);
         buttonPanel.add(modID);
         buttonPanel.add(submitQuery);
         buttonPanel.add(disconnect);
 
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g;
         if (regPoints != null) {
             int[] axRanges = getRanges();
             // max year
 //            g2.drawString(String.valueOf(axRanges[0]), (float) bottomRight.getX()-20, (float) bottomRight.getY() + 25);
             // min year
 //            g2.drawString(String.valueOf(axRanges[1]), (float) origin.getX() - 5, (float) origin.getY() + 25);
             // max students
 //            g2.drawString(String.valueOf(axRanges[2]), (float) topLeft.getX() - 20, (float) topLeft.getY() + 8);
             // min students
 //            g2.drawString(String.valueOf(axRanges[3]), (float) origin.getX() - 20, (float) origin.getY() + 8);
 
             int maxStudents = axRanges[2];
 
             int numYears = regPoints.size();
             if (numYears == 0 || maxStudents == 0) {
                 g2.setColor(Color.red);
                 g2.drawString("NO DATA", 300, 400);
                 g2.setColor(Color.black);
             } else {
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 Point2D topLeft = new Point2D.Double(70, 90);
                 Point2D origin = new Point2D.Double(70, 515);
                 Point2D bottomRight = new Point2D.Double(720, 515);
 
                 Line2D leftAxis = new Line2D.Double(topLeft, origin);
                 g2.draw(leftAxis);
                 Line2D bottomAxis = new Line2D.Double(origin, bottomRight);
                 g2.draw(bottomAxis);
 
                 // student label
                 g2.drawString("Students", (float) topLeft.getX() - 60, (float) (origin.getY() - topLeft.getY()) / 2);
                 // year label
                 g2.drawString("Year", (float) (bottomRight.getX() - origin.getX()) / 2, (float) bottomRight.getY() + 40);
 
                 int bottomAxisLength = (int) (bottomRight.getX() - origin.getX());
                 int leftAxisLength = (int) (origin.getY() - topLeft.getY());
                 int studentMarks = 4;
                 int studentsSplit = leftAxisLength / studentMarks;
                 int yearsSplit = bottomAxisLength / (numYears - 1);
 
                 System.out.printf("bottom %d, lreft %d, stud %d,  year%d\n", bottomAxisLength, leftAxisLength, studentsSplit, yearsSplit);
 
                 Point lastPoint = new Point((int) origin.getX(), (int) origin.getY());
                 Point currentPoint;
                 for (int i = 0; i < regPoints.size(); i++) {
                     // point at which the marker is at the bottom axis
                     double xPoint = origin.getX() + (i * yearsSplit);
                     // Drawing marks for each year
                     Line2D yearMark = new Line2D.Double(xPoint, origin.getY(), xPoint, origin.getY() + 10);
                     g2.draw(yearMark);
                     g2.drawString(String.valueOf(regPoints.get(i).x), (int) (xPoint - 15), (int) (origin.getY() + 25));
                     g2.setColor(Color.red);
                     currentPoint = new Point((int) xPoint, (int) (origin.getY() - getStudentPointY(leftAxisLength, maxStudents, regPoints.get(i).y)));
                     g2.fillOval((int) (xPoint - 2), (int) (origin.getY() - getStudentPointY(leftAxisLength, maxStudents, regPoints.get(i).y) - 2), 5, 5);
                     g2.drawString(String.valueOf(regPoints.get(i).y), currentPoint.x, currentPoint.y);
                     g2.setColor(Color.black);
 
                     g2.draw(new Line2D.Double(lastPoint, currentPoint));
                     lastPoint = currentPoint;
                 }
                 for (int i = 0; i <= studentMarks; i++) {
                     // left axis marker point
                     double yPoint = origin.getY() - ((i) * studentsSplit);
                     // drawing marks for student steps
                     Line2D studentMark = new Line2D.Double(origin.getX(), yPoint, origin.getX() - 10, yPoint);
 //                    System.out.printf("current i: %d, 1/i+1: %f, max students: %d", i, (1.0/(i)), maxStudents);
                     double i1 = i;
                     double m = studentMarks;
                     double markvalue = i1 / m * maxStudents;
                     System.out.printf("%f\n", markvalue);
                     g2.drawString(String.valueOf(markvalue), (int) (origin.getX() - 30), (int) (yPoint + 5));
                     g2.draw(studentMark);
                 }
             }
         }
     }
 
     public int getStudentPointY(int leftAxisLength, int maxStudents, int studentNumber) {
 //        System.out.printf("number %d, max %d, axis %d, position %f\n", studentNumber, maxStudents, leftAxisLength, ((double) studentNumber / (double) maxStudents) * leftAxisLength);
         double s = (double) studentNumber / (double) maxStudents;
 //        System.out.printf("%f ", s);
         return (int) (s * leftAxisLength);
     }
 
     /**
      * gets the min an max values for the axes to be at.
      * @return
      */
     public int[] getRanges() {
         if (regPoints != null) {
             int maxYr = Integer.MIN_VALUE;
             int minYr = Integer.MAX_VALUE;
             int maxStud = Integer.MIN_VALUE;
             int minStud = Integer.MAX_VALUE;
             // y point is student count, x is year
             for (Point point : regPoints) {
                 if (point.x > maxYr) {
                     maxYr = point.x;
                 }
                 if (point.x < minYr) {
                     minYr = point.x;
                 }
 
                 if (point.y > maxStud) {
                     maxStud = point.y;
                 }
                 if (point.y < minStud) {
                     minStud = point.y;
                 }
             }
             System.out.printf("mxyr %d, minyr %d, maxst %d, minst %d\n", maxYr, minYr, maxStud, minStud);
             return new int[]{maxYr, minYr, maxStud, minStud};
         }
 
         return null;
     }
 
     public void getRegistrationPoints(String modiD) {
         gClient.getRegistrationPoints(modiD);
     }
 
     public void actionPerformed(ActionEvent e) {
         String pressed = e.getActionCommand();
         if (pressed.equals("Login")) {
             JTextField password = (JTextField) textFieldMap.get("pass");
             JTextField username = (JTextField) textFieldMap.get("user");
             String userText = username.getText();
             String passText = password.getText();
             if (userText.equals("") || passText.equals("")) {
                 JOptionPane.showMessageDialog(this, "You need to enter a username and password.");
                 return;
             } else {
                 if (!gClient.auth) {
                     doServerAuth(passText, userText);
                 }
             }
         } else if (pressed.equals("Get Data")) {
             if (!authenticated) {
                 JOptionPane.showMessageDialog(buttonPanel, "You've not yet authenticated.");
             }
             JTextField module = (JTextField) textFieldMap.get("module");
             String modText = module.getText();
             if (modText.equals("")) {
                 JOptionPane.showMessageDialog(this, "You haven't input any data.");
                 return;
             } else {
                 regPoints = gClient.getRegistrationPoints(modText);
                 System.out.println(regPoints);
                 repaint();
             }
         } else if (pressed.equals("Disconnect")) {
             gClient.disconnect();
         }
     }
 
     public void doServerAuth(String user, String password) {
        System.out.println("handshaked " + gClient.handshaked);
         if (!gClient.handshaked) {
             gClient.handshakeServ();
        }
        boolean auth = gClient.authenticate(user, password);
        if (auth) {
            authenticated = true;
            JOptionPane.showMessageDialog(buttonPanel, "Successfully authenticated.");
            return;
         }
         authenticated = false;
 
         JOptionPane.showMessageDialog(buttonPanel, "Authentication failed.");
     }
 }
