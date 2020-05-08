 package model;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.util.Vector;
 
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.util.PDFTextStripper;
 /**
  * 
  * ReadPDF.java
  * 
  * 
  * @author Fadi M.H.Asbih
  * @email fadi_asbih@yahoo.de
  * @version 1.2.0  04/02/2012
  * @copyright 2012
  * 
  * TERMS AND CONDITIONS:
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 public class ReadPDF {
 
         Vector<String> courses = new Vector();
         private String finalGrade;
         private String credits;
         private String numberOfSubjects;
         private String numberOfSubjectsWithGrade;
         private String startThesis;
         private String subject;
         private String certificate;
         private double procent;
         
         public String getNumberOfSubjects() {
                 return numberOfSubjects;
         }
 
         public void setNumberOfSubjects(String numberOfSubjects) {
                 this.numberOfSubjects = numberOfSubjects;
         }
 
         public String getNumberOfSubjectsWithGrade() {
                 return numberOfSubjectsWithGrade;
         }
 
         public void setNumberOfSubjectsWithGrade(String numberOfSubjectsWithGrade) {
                 this.numberOfSubjectsWithGrade = numberOfSubjectsWithGrade;
         }
 
         public String getCredits() {
                 return credits;
         }
 
         public void setCredits(String credits) {
                 this.credits = credits;
         }
 
         public ReadPDF() {
                 
         }
         
         public void ReadPDF(String file) throws Exception {
                 PDDocument pddDocument=PDDocument.load(new File(file));
                 PDFTextStripper textStripper=new PDFTextStripper();
                 
                 this.setStartThesis("noch nicht mglich");
                 
                 double not=0;
                 double sumCredit = 0;
                 double s=0;
                 double s1=0;
                 double sumcredit = 0;
                 int rankednumberOfSubjects=0;
                 int numberOfSubjects=0;
                 DecimalFormat df = new DecimalFormat("0.00");
                 String x = textStripper.getText(pddDocument);
                 String[] lines = getLines(x);
                 
                 // find all passed rankedNumberOfSubjects and save them in a Vector.
                 for(int i=0; i<lines.length; i++) {
 //                	System.out.println(lines[i]);
                 	if(lines[i].contains("Fach:")) {
                 		setSubject(lines[i]);
                 	}
                 	if(lines[i].contains("Abschluss:")) {
                 		setCertificate(lines[i]);
                 	}
                 	if(lines[i].contains("BE")) {
                 		courses.addElement(lines[i]);
                     }
                 }
 //                System.out.println(getSubject() +" "+getCertificate());
                 for(int i=0; i<courses.size(); i++) {
                         
                         // first Check if the line is an Exam.
                         if(isExam(courses, i)) {
                                 
                                 // second Check if the Exam is rated.
                                 if(isRated(courses, i)) {
                                         
                                         // Get the mark of the Subject.
                                         String mark = getMark(courses, i);
                                         // Count the number of the rankednumberOfSubjects.
                                         rankednumberOfSubjects++;
                                         // Count the number of the numberOfSubjects.
                                         numberOfSubjects++;
                                         // Get the Credit Points of the Subject.
                                         String credit = getCredit(courses, i);
                                         
                                         not = Double.parseDouble(mark);
                                         sumCredit = Double.parseDouble(credit);
                                 
                                         s1 = s1 + not*sumCredit;
                                         s = s + sumCredit;
                                         sumcredit = sumcredit + sumCredit;
                                         
                                         // Debug
 //                                        System.out.println("Note: "+mark);
 //                                        System.out.println("Credit: " + credit);
 //                                        System.out.println(courses.elementAt(i));
                                 }
                                 else { // Not Rated Exams.
                                         numberOfSubjects++;
                                         String credit = getCredit(courses, i);
                                         sumCredit = Double.parseDouble(credit);
                                         sumcredit = sumcredit + sumCredit;
                                         
                                         // Debug
 //                                        System.out.println("Credit: " + credit);
 //                                        System.out.println(courses.elementAt(i));
                                 }
                         }
                 }
                                 
                 setFinalGrade(df.format(s1/s)+"");
                 setCredits((int)sumcredit+"");
                 setNumberOfSubjects(numberOfSubjects+"");
                 
                 if(getCertificate().contains("Master")) {
                 	setProcent((sumcredit/120)*100);
                 	if((int)sumcredit >= 75)
                 		this.setStartThesis("mglich");
                 }
                 else if(getCertificate().contains("Bachelor")) {
                 	setProcent((sumcredit/180)*100);
                 	if((int)sumcredit >= 140) 
                 		this.setStartThesis("mglich");
                 }
               
                 setNumberOfSubjectsWithGrade(rankednumberOfSubjects+"");
                 pddDocument.close();    
         }
         
         public String[] getLines(String input) {
                 String[] lines = input.split("\n");
                 return lines;
         }
 
         public String getFinalGrade() {
                 return finalGrade;
         }
 
         public void setFinalGrade(String finalGrade) {
                 this.finalGrade = finalGrade;
         }
         
         /**
          * check if the Parsed lines are Exams, not i.e. Titles or adresses.
          * 
          * @param vector
          * @param index
          * @return
          */
         public boolean isExam(Vector<String> vector, int index) {
                 if(vector.elementAt(index).indexOf("PL") > -1)
                         return true;
                 if(vector.elementAt(index).indexOf("SL") > -1)
                         return true;
                 if(vector.elementAt(index).indexOf("WL") > -1)
                         return true;
                 if(vector.elementAt(index).indexOf("PR") > -1)
                         return true;
                 if(vector.elementAt(index).indexOf("GL") > -1)
                     return true;
                 else
                         return false;
         }
         /**
          * check if the Exam is a rated or not.
          * 
          * @param vector
          * @param index
          * @return
          */
         public boolean isRated(Vector<String> vector, int index) {
                 if(vector.elementAt(index).contains(","))
                         return true;
                 else
                         return false;
         }
         
         /**
          * get the Credit Points for the Exam.
          * 
          * @param vector
          * @param index
          * @return
          */
         public String getCredit(Vector<String> vector, int index) {
                int startCreditPosition = vector.elementAt(index).lastIndexOf("BE")+3;
                int endCreditPosition = vector.elementAt(index).lastIndexOf("BE")+5;
                 String credit = vector.elementAt(index).substring(startCreditPosition,endCreditPosition);
                 
                 return credit;
         }
         
         /**
          * if Exam is rated, get the mark of that Exam.
          * 
          * @param vector
          * @param index
          * @return
          */
         public String getMark(Vector<String> vector, int index) {
                 // find the start & end mark position of the Exam.
                 int startMarkPosition = vector.elementAt(index).lastIndexOf("B")-4;
                 int endMarkPosition = vector.elementAt(index).lastIndexOf("B")-1;
 
                 // Replace the Comma with a dot in order to be able to calculate.
                 String mark = vector.elementAt(index).substring(startMarkPosition,endMarkPosition).replace(',', '.');
                 return mark;
         }
 
 		public double getProcent() {
 			return procent;
 		}
 
 		public void setProcent(double procent) {
 			this.procent = procent;
 		}
 
 		public String getSubject() {
 			return subject;
 		}
 
 		public void setSubject(String subject) {
 			this.subject = subject;
 		}
 
 		public String getCertificate() {
 			return certificate;
 		}
 
 		public void setCertificate(String certificate) {
 			this.certificate = certificate;
 		}
 
 		public String getStartThesis() {
 			return startThesis;
 		}
 
 		public void setStartThesis(String startThesis) {
 			this.startThesis = startThesis;
 		}
         
 }
