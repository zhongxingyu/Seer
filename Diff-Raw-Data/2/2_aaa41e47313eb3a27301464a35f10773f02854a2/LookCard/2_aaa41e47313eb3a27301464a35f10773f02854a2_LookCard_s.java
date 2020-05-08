 package de.team55.mms.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.plaf.basic.BasicTreeUI;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreeSelectionModel;
 
 import de.team55.mms.data.Fach;
 import de.team55.mms.data.Feld;
 import de.team55.mms.data.Modul;
 import de.team55.mms.data.Modulhandbuch;
 import de.team55.mms.data.Studiengang;
 import de.team55.mms.function.ServerConnection;
 
 import com.itextpdf.text.Chapter;
 import com.itextpdf.text.Chunk;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.FontFactory;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Section;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.itextpdf.text.pdf.draw.VerticalPositionMark;
 
 public class LookCard extends JPanel {
 	private static JFrame frame;
 	private ServerConnection serverConnection = null;
 	private static JPanel looking = new JPanel();
 	private static JPanel content = new JPanel();
 	private static JPanel btn_panel = new JPanel();
 	private JTree tree;
 	private ArrayList<Studiengang> studienlist = null;
 	private DefaultMutableTreeNode node;
 	private JButton pdfbtn;
 
 	public LookCard() {
 		super();
 		this.setLayout(new BorderLayout(0, 0));
 		add(looking, BorderLayout.CENTER);
 		looking.setLayout(new BorderLayout(0, 0));
 		content.setLayout(new BorderLayout(0, 0));
 		btn_panel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		looking.add(content, BorderLayout.CENTER);
 		looking.add(btn_panel, BorderLayout.SOUTH);
 		pdfbtn = new JButton("PDF ausgeben");
 
 	}
 
 	public void buildTree() {
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Universitt Ulm");
 		DefaultMutableTreeNode child;
 		DefaultMutableTreeNode grandChild;
 		DefaultMutableTreeNode t3Child;
 		DefaultMutableTreeNode t4Child;
 
 		for (int i = 0; i < studienlist.size(); i++) {
 			child = new DefaultMutableTreeNode(studienlist.get(i).toString());
 			root.add(child);
 			for (int j = 0; j < studienlist.get(i).getModbuch().size(); j++) {
 				grandChild = new DefaultMutableTreeNode("Modulhandbuch " + studienlist.get(i).getModbuch().get(j).getJahrgang() + " PO "
 						+ studienlist.get(i).getModbuch().get(j).getPruefungsordnungsjahr());
 				child.add(grandChild);
 				for (int k = 0; k < studienlist.get(i).getModbuch().get(j).getFach().size(); k++) {
 					t3Child = new DefaultMutableTreeNode(studienlist.get(i).getModbuch().get(j).getFach().get(k).getName());
 					grandChild.add(t3Child);
 					for (int l = 0; l < studienlist.get(i).getModbuch().get(j).getFach().get(k).getModlist().size(); l++) {
 						t4Child = new DefaultMutableTreeNode(studienlist.get(i).getModbuch().get(j).getFach().get(k).getModlist().get(l)
 								.getName());
 						t3Child.add(t4Child);
 
 					}
 				}
 			}
 		}
 
 		tree = new JTree(root);
 		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
 			{
 				setLeafIcon(new ImageIcon("leafIcon.jpg"));
 				setOpenIcon(new ImageIcon("openIcon.jpg"));
 				setClosedIcon(new ImageIcon("closeIcon.jpg"));
 			}
 		};
 
 		BasicTreeUI ui = (BasicTreeUI) tree.getUI();
 		tree.setCellRenderer(renderer);
 		content.add(tree);
 
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		tree.addTreeSelectionListener(new TreeSelectionListener() {
 
 			@Override
 			public void valueChanged(TreeSelectionEvent e) {
 				// TODO Auto-generated method stub
 				node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
 			}
 		});
 
 		btn_panel.add(pdfbtn);
 		pdfbtn.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				if (node != null) {
 					// System.out.println(node.toString());
 					if (node.getParent() != null)
 						// System.out.println(node.getParent().toString());
 						if ((node.getParent().toString().equalsIgnoreCase("Universitt Ulm") || node.getParent().getParent().toString()
 								.equalsIgnoreCase("Universitt Ulm"))) {
 							if (node.getParent().toString().equalsIgnoreCase("Universitt Ulm")) {
 								toPDF(node.toString());
 							} else {
 								toPDF(node.getParent().toString(), node.toString());
 							}
 						} else {
 							JOptionPane.showMessageDialog(frame, "Bitte whlen sie entweder einen Studiengang oder ein Modulhandbuch aus.",
 									"Input Error", JOptionPane.ERROR_MESSAGE);
 
 						}
 				}
 			}
 		});
 
 	}
 
 	public void setConnection(ServerConnection serverConnection) {
 		this.serverConnection = serverConnection;
 	}
 
 	public void setStudienlist(ArrayList<Studiengang> studienlist) {
 		this.studienlist = studienlist;
 	}
 
 	public void toPDF(String st) {
 
 		int stu = 9001;
 		for (int i = 0; i < studienlist.size(); i++) {
 			if (studienlist.get(i).toString().equalsIgnoreCase(st))
 				stu = i;
 		}
 		aMHBPDF(stu);
 	}
 
 	public void toPDF(String st, String mo) {
 		int stu = 9001, mod = -1;
 
 		for (int i = 0; i < studienlist.size(); i++) {
 			if (studienlist.get(i).toString().equalsIgnoreCase(st))
 				stu = i;
 		}
 		for (int j = 0; j < studienlist.get(stu).getModbuch().size(); j++) {
 
 			String ja = studienlist.get(stu).getModbuch().get(j).getJahrgang();
 			int po = studienlist.get(stu).getModbuch().get(j).getPruefungsordnungsjahr();
 			String could = "Modulhandbuch " + ja + " PO " + po;
 			if (could.equalsIgnoreCase(mo))
 				mod = j;
 		}
 
 		try {
 			MHBPDF(stu, mod);
 		} catch (IOException | DocumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void aMHBPDF(int stu) {
 
 		for (int i = 0; i < studienlist.get(stu).getModbuch().size(); i++) {
 			try {
 				MHBPDF(stu, i);
 			} catch (IOException | DocumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	@SuppressWarnings("deprecation")
 	public void MHBPDF(int stu, int mod) throws IOException, DocumentException {
 
 		Paragraph tab =(new Paragraph (new Chunk(new VerticalPositionMark(), 80)));// anpassen
 		// Chunk underline = new Chunk("Underline. ");
 		// underline.setUnderline(0.1f, -2f); // 0.1 thick, -2 y-location
 
 		Font font = FontFactory.getFont("Times-New-Roman", 12, Font.NORMAL);
 		Font fontbold = FontFactory.getFont("Times-New-Roman", 16, Font.BOLD);
 		Font fontunder = FontFactory.getFont("Times-New-Roman", 14, Font.UNDERLINE);
 		Font fontunderBig = FontFactory.getFont("Times-Roman", 20, Font.UNDERLINE);
 
 		// document.add(new Paragraph("Times-Roman, Bold", fontbold));
 		Paragraph cTitle, par, st;
 		Chapter chapter;
 		Section section;
 		// Section subsection;
 
 		String studname = studienlist.get(stu).getName();
 		String abschluss = studienlist.get(stu).getAbschluss();
 
 		Modulhandbuch ModHB = studienlist.get(stu).getModbuch().get(mod);
 
 		String prosa = ModHB.getProsa();
 		String jahrgang = ModHB.getJahrgang();
 		int pruefjahr = ModHB.getPruefungsordnungsjahr();
 
 		String pdfname = abschluss + "-" + studname + "-PO" + pruefjahr + "-" + jahrgang;
		String titel = ("Modulhandbuch: " + abschluss + "" + studname + " FSPO " + pruefjahr + " " + jahrgang);
 
 		Document document = new Document();
 
 		// Writer Instanz erstellen
 		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfname + ".pdf"));
 
 		writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
 
 		// step 3: Dokument ffnen
 		document.open();
 		// step 4: Absatz mit Text dem Dokument hinzufgen
 		document.add(new Paragraph(titel, fontunderBig));
 		
 		document.add(Chunk.NEWLINE);
 		document.add(Chunk.NEWLINE);
 		document.add(Chunk.NEWLINE);
 		document.add(new Paragraph(prosa, font));
 
 		for (int j = 0; j < ModHB.getFach().size(); j++) {
 
 			Fach Fach = ModHB.getFach().get(j);
 			String fachname = Fach.getName();
 
 			cTitle = new Paragraph(fachname, fontbold);
 			chapter = new Chapter(cTitle, j+1);
 			par = new Paragraph("");
 
 			chapter.add(Chunk.NEWLINE);//document.add(Chunk.NEWLINE); // leerzeile
 
 			for (int k = 0; k < Fach.getModlist().size(); k++) {
 				Modul Dul = Fach.getModlist().get(k);
 
 				String Modname = Dul.getName();
 
 				section = chapter.addSection((new Paragraph(Modname, fontunder)), 2);
 				section.setBookmarkOpen(false);
 				section.add(Chunk.NEWLINE);
 
 				for (int l = 0; l < Dul.getFelder().size(); l++) {
 
 					Feld Felder = Dul.getFelder().get(l);
 
 					String label = Felder.getLabel();
 					String value = Felder.getValue();
 
 					section.add(new Paragraph((label + ": "+ value), font));
 				}// 3 fs
 
 				section.newPage();
 			}// f-shleif 2
 
 			document.add(chapter);
 			
 		}// erste for-schleife
 
 		document.close();
 	}
 
 }
