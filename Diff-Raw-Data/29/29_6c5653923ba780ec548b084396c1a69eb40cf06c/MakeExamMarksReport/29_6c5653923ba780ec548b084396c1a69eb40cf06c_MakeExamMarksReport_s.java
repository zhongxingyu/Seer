 package kg.cloud.uims.util;
 
 import kg.cloud.uims.MyVaadinApplication;
 import kg.cloud.uims.dao.DbStudAccounting;
 import kg.cloud.uims.dao.DbSubjExam;
 import kg.cloud.uims.dao.DbSubjects;
 import kg.cloud.uims.domain.StudAccounting;
 import kg.cloud.uims.domain.SubjExam;
 import kg.cloud.uims.domain.Subjects;
 
 import com.lowagie.text.Document;
 import com.lowagie.text.Element;
 import com.lowagie.text.Font;
 import com.lowagie.text.PageSize;
 import com.lowagie.text.Paragraph;
 import com.lowagie.text.pdf.PdfWriter;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.vaadin.terminal.StreamResource;
 import java.awt.Color;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import com.lowagie.text.Image;
 import com.lowagie.text.Phrase;
 import com.lowagie.text.pdf.PdfPTable;
 import java.util.ArrayList;
 
 
 public class MakeExamMarksReport {
 
 	private byte[] b = null;
 	private StreamResource.StreamSource source1 = null;
 	ByteArrayOutputStream buffer = null;
 	StreamResource resource = null;
 	private Image img;
 
 	private String subjectId = null;
 	private ArrayList<SubjExam> studLessList;
 	private ArrayList<Subjects> subj;
 	private MyVaadinApplication app;
 	private String year_id, sem_id, exam_id, exam_name,exam_mark;
 
 	public MakeExamMarksReport(final MyVaadinApplication app, String subj_id,
 			String e_id, String e_name) {
 		this.app = app;
 		this.subjectId = subj_id;
 		this.exam_id = e_id;
 		this.exam_name = e_name;
 		this.sem_id = Integer.toString(app.getCurrentSemester().getId());
 		this.year_id = Integer.toString(app.getCurrentYear().getId());
 
 		source1 = new StreamResource.StreamSource() {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 			Document document = new Document(PageSize.A4, 10, 10, 10, 10);
 
 			public InputStream getStream() {
 
 				buffer = new ByteArrayOutputStream();
 
 				try {
 
 					DbSubjExam dbSubjExam = new DbSubjExam();
 					dbSubjExam.connect();
 					dbSubjExam.execSQL(subjectId, year_id, sem_id, exam_id);
 					studLessList = dbSubjExam.getArray();
 					dbSubjExam.close();
 
 					DbSubjects dbsubject = new DbSubjects();
 					dbsubject.connect();
 					dbsubject.execSQL_subj(subjectId);
 					subj = dbsubject.getArray();
 					dbsubject.close();
 					
 					DbStudAccounting dbAccounting = new DbStudAccounting();
 					dbAccounting.connect();
 
 					PdfWriter writer = PdfWriter.getInstance(document, buffer);
 
 					document.open();
 					PdfContentByte punder = writer.getDirectContentUnder();
 					img = Image.getInstance("/usr/local/images/iaauLogoT.png");
 					img.setAbsolutePosition(
 							document.getPageSize().getWidth() / 4, document
 									.getPageSize().getHeight() / 3);
 					img.scaleAbsolute(300, 300);
 					punder.addImage(img);
 					Font big_font = new Font(Font.COURIER, 19, Font.BOLD);
 					big_font.setColor(new Color(0x92, 0x90, 0x83));
 					Font title_font = new Font(Font.COURIER, 13, Font.BOLD);
 					title_font.setColor(new Color(0x92, 0x90, 0x83));
 					Font warning = new Font(Font.COURIER, 10, Font.BOLD);
 					warning.setColor(new Color(0xFF, 0x00, 0x00));
 					Font in_font = new Font(Font.COURIER, 12, Font.BOLD);
 					Font text_font = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL);
 					Paragraph iaau = new Paragraph(
 							"INTERNATIONAL ATATURK ALATOO UNIVERSITY",
 							title_font);
 					iaau.setAlignment(Element.ALIGN_CENTER);
 					Paragraph sif = new Paragraph("EXAMINATION RESULT LIST",
 							big_font);
 					sif.setAlignment(Element.ALIGN_CENTER);
 					Paragraph note = new Paragraph("* WBI - Exam results ignored students must visit " +
 							"Accounting Department between May 28 and June 08, 2012",
 							in_font);
 					note.setAlignment(Element.ALIGN_CENTER);
 					document.add(iaau);
 					document.add(sif);
 					document.add(new Paragraph(10, " "));
 					if (!studLessList.isEmpty()) {
 						float[] Thead_colsWidth = { 1.2f, 1.5f, 0.8f, 1.5f };
 						PdfPTable Thead = new PdfPTable(4);
 						Thead.setWidthPercentage(90f);
 						Thead.setWidths(Thead_colsWidth);
 						Thead.getDefaultCell().setBorder(0);
 						Thead.addCell(new Phrase("Department:", in_font));
 						Thead.addCell(new Phrase(subj.get(0).getDeptName(),
 								text_font));
 						Thead.addCell(new Phrase("Subject:", in_font));
 						Thead.addCell(new Phrase(studLessList.get(0)
 								.getSubjectName(), text_font));
 						Thead.addCell(new Phrase("Academic Year:", in_font));
 						Thead.addCell(new Phrase(
 								app.getCurrentYear().getYear(), text_font));
 						Thead.addCell(new Phrase("Semester:", in_font));
 						Thead.addCell(new Phrase(app.getCurrentSemester()
 								.getSemester(), text_font));
 						Thead.addCell(new Phrase("Examination:", in_font));
 						Thead.addCell(new Phrase(exam_name, text_font));
 						Thead.addCell(new Phrase("Date: ", in_font));
 						Thead.addCell(new Phrase(" "));
 						document.add(Thead);
 						document.add(new Paragraph(10, " "));
 						float[] Tbody_colsWidth = { 0.1f, 0.8f, 0.5f, 0.5f };
 						PdfPTable Tbody = new PdfPTable(4);
 						Tbody.setWidthPercentage(90f);
 						Tbody.setWidths(Tbody_colsWidth);
 						Tbody.getDefaultCell().setFixedHeight(16);
 						Tbody.getDefaultCell().setHorizontalAlignment(
 								Element.ALIGN_CENTER);
 						Tbody.addCell(new Phrase("#", in_font));
 						Tbody.addCell(new Phrase("Roll Number", in_font));
 						Tbody.addCell(new Phrase("Group", in_font));
 						Tbody.addCell(new Phrase("Mark", in_font));
 						for (int i = 0; i < studLessList.size(); i++) {
 							String stud_id = Integer.toString(studLessList.get(
 									i).getStudID());
 							dbAccounting.execSQL(stud_id, sem_id, year_id);
 							ArrayList<StudAccounting> account = dbAccounting
 									.getArray();
 							Tbody.addCell(new Phrase(Integer.toString(i + 1),
 									text_font));
 							Tbody.getDefaultCell().setHorizontalAlignment(
 									Element.ALIGN_LEFT);
							if (account.isEmpty()
 									|| ((account.get(0).getFinStatus() != 1 && exam_id.equals("2")))
 									|| ((account.get(0).getMidStatus() != 1 && exam_id.equals("1")))) {
 								Tbody.addCell(new Phrase("*"
 										+ studLessList.get(i).getStRollNum(),
 										text_font));
 								exam_mark="WBI";
 								
 							} else {
 								Tbody.addCell(new Phrase(studLessList
 										.get(i).getStRollNum(),
 										text_font));
 	
 								exam_mark=Integer.toString(studLessList.get(i)
 										.getExamMark());
 							}
 							
 							Tbody.getDefaultCell().setHorizontalAlignment(
 									Element.ALIGN_CENTER);
 							Tbody.addCell(new Phrase(studLessList.get(i)
 									.getGroup(), text_font));
 							Tbody.addCell(new Phrase(exam_mark, text_font));
 						}
 						document.add(Tbody);
 						document.add(new Paragraph(10, " "));
 						float[] Tfoot_colsWidth = { 1.5f, 1f };
 						PdfPTable Tfoot = new PdfPTable(2);
 						Tfoot.setWidthPercentage(90f);
 						Tfoot.setWidths(Tfoot_colsWidth);
 						Tfoot.getDefaultCell().setHorizontalAlignment(
 								Element.ALIGN_LEFT);
 						Tfoot.addCell(new Phrase("Name Surname : "
 								+ app.getInstName() + " "
 								+ app.getInstSurname(), in_font));
 						Tfoot.addCell(new Phrase("Signature :", in_font));
 						document.add(Tfoot);
 						document.add(new Paragraph(10, " "));
 						document.add(note);
 					} else {
 						document.add(new Phrase("No records found", warning));
 					}
 
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				} finally {
 					if (document != null) {
 						document.close();
 					}
 				}
 
 				b = buffer.toByteArray();
 				return new ByteArrayInputStream(b);
 
 			}
 		};
 
 		// Embedded e = new Embedded();
 		// e.setSizeFull();
 		// e.setType(Embedded.TYPE_BROWSER);
 
 		resource = new StreamResource(source1, "TokenReport.pdf"
 				+ System.currentTimeMillis(), app);
 		resource.setMIMEType("application/pdf");
 
 		// e.setSource(resource);
 		// e.setParameter("Content-Disposition", "attachment; filename=" +
 		// resource.getFilename());
 
 		// app.addComponent(e);
 		app.getMainWindow().open(resource);
 	}
 }
