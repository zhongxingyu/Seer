 package ModelBuilder;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 
 import org.jdom.Element;
 
 import GeneticModels.ProblemTypeSpecification;
 import VGL.Messages;
 
 public class ModelPane extends JPanel implements ItemListener {
 
 	private int index;
 	private String character;
 	private String[] traits;
 	private ProblemTypeSpecification specs;
 	private ModelBuilderUI modelBuilderUI;
 
 	private JComboBox sexLinkageChoices;
 	private JComboBox alleleNumberChoices;
 	private JComboBox interactionTypeChoices;
 	private JComboBox t1Choices;
 	private JComboBox t2Choices;
 	private JComboBox t3Choices;
 	private JComboBox t4Choices;
 	private JComboBox t5Choices;
 	private JComboBox t6Choices;
 
 	private boolean circularPossible;
 	private boolean incDomPossible;
 	private boolean complementationPossible;
 	private boolean epistasisPossible;
 
 	private JPanel interactionTypePanel;
 	private JPanel interactionDetailsPanel;
 	
 	// values updated by the model details panels
 	private String t1Value;
 	private String t2Value;
 	private String t3Value;
 	private String t4Value;
 	private String t5Value;
 	private String t6Value;
 
 
 	public ModelPane(int index, 
 			String character,
 			String[] traits, 
 			ProblemTypeSpecification specs,
 			ModelBuilderUI modelBuilderUI) {
 		this.index = index;
 		this.character = character;
 		this.traits = traits;
 		this.specs = specs;
 		this.modelBuilderUI = modelBuilderUI;
 		setupUI();
 	}
 
 	private void setupUI() {
 		JPanel masterPanel = new JPanel();
 		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
 		masterPanel.add(Box.createRigidArea(new Dimension(300,1)));
 
 		// sex linkage info
 		JPanel sexLinkagePanel = new JPanel();
 		sexLinkagePanel.setBorder(
 				BorderFactory.createTitledBorder(
 						Messages.getInstance().getString("VGLII.SexLinkage")));
 		ArrayList<String>sexLinkageChoiceStrings = new ArrayList<String>();
 		if (specs.getGene1_chSexLinked() > 0.0) {
 			sexLinkageChoiceStrings.add(
 					Messages.getInstance().getString(
 					"VGLII.Unknown"));
 			sexLinkageChoiceStrings.add(Messages.getInstance().getString("VGLII.NotSexLinked"));
 			sexLinkageChoiceStrings.add("XX " 
 					+ Messages.getInstance().getString("VGLII.Female")
 					+ "/XY "
 					+ Messages.getInstance().getString("VGLII.Male"));
 			if (specs.getChZZ_ZW() > 0.0) {
 				sexLinkageChoiceStrings.add("ZZ " 
 						+ Messages.getInstance().getString("VGLII.Male")
 						+ "/ZW "
 						+ Messages.getInstance().getString("VGLII.Female"));
 			}
 		} else {
 			sexLinkageChoiceStrings.add(Messages.getInstance().getString("VGLII.NotSexLinked"));
 		}
 		String[] slcs = new String[sexLinkageChoiceStrings.size()];
 		for (int i = 0; i < slcs.length; i++) {
 			slcs[i] = sexLinkageChoiceStrings.get(i);
 		}
 		sexLinkageChoices = new JComboBox(slcs);
 		sexLinkagePanel.add(sexLinkageChoices);
 		masterPanel.add(sexLinkagePanel);
 
 
 		// number of alleles
 		JPanel alleleNumberChoicePanel = new JPanel();
 		alleleNumberChoicePanel.setBorder(
 				BorderFactory.createTitledBorder(
 						Messages.getInstance().getString("VGLII.NumberOfAlleles")));
 		String[] alleleNumberStrings;
 		boolean isOnly2Alleles;
 		if ((specs.getGene1_ch3Alleles() > 0.0) ||
 				(specs.getGene2_ch3Alleles() > 0.0) ||
 				(specs.getGene3_ch3Alleles() > 0.0)) {
 			isOnly2Alleles = false;
 			alleleNumberStrings = new String[3];
 			alleleNumberStrings[0] = Messages.getInstance().getString("VGLII.Unknown");
 			alleleNumberStrings[1] = "2-" + Messages.getInstance().getString("VGLII.Allele");
 			alleleNumberStrings[2] = "3-" + Messages.getInstance().getString("VGLII.Allele");
 		} else {
 			isOnly2Alleles = true;
 			alleleNumberStrings = new String[1];
 			alleleNumberStrings[0] = "2-" + Messages.getInstance().getString("VGLII.Allele");
 		}
 		alleleNumberChoices = new JComboBox(alleleNumberStrings);
 		alleleNumberChoicePanel.add(alleleNumberChoices);
 		masterPanel.add(alleleNumberChoicePanel);
 
 		// allele interaction type
 		circularPossible = false;
 		if ((specs.getGene1_chCircDom() != 0.0)
 				|| (specs.getGene2_chCircDom() != 0.0)
 				|| (specs.getGene3_chCircDom() != 0.0)) circularPossible = true;
 		
 		incDomPossible = false;
 		if ((specs.getGene1_chIncDom() != 0.0)
 				|| (specs.getGene2_chIncDom() != 0.0)
 				|| (specs.getGene3_chIncDom() != 0.0)) incDomPossible = true;
 		
 		complementationPossible = false;
 		if ((specs.getPhenotypeInteraction() > 0.0)
 				&& (specs.getEpistasis() != 1.0)) complementationPossible = true;
 		
 		epistasisPossible = false;
 		if ((specs.getPhenotypeInteraction() > 0.0)
 				&& (specs.getEpistasis() != 0.0)) epistasisPossible = true;
 
 		interactionTypePanel = new JPanel();
 		interactionTypePanel.setBorder(
 				BorderFactory.createTitledBorder(
 						Messages.getInstance().getString("VGLII.GeneralAllelicInteractions")));
 		if (isOnly2Alleles) {
 			interactionTypePanel.removeAll();
 			TwoAllelePanel twap = new TwoAllelePanel(
 					incDomPossible, complementationPossible, epistasisPossible);
 			interactionTypeChoices = twap.getInteractionTypeChoices();
 			interactionTypeChoices.addItemListener(this);
 			interactionTypePanel.add(twap);
 			interactionTypePanel.revalidate();				
 		} else {
 			interactionTypePanel.add(new UnknownInteractionPanel());
 		}
 
 		masterPanel.add(interactionTypePanel);
 
 		// allele interaction details
 		interactionDetailsPanel = new JPanel();
 		interactionDetailsPanel.setBorder(
 				BorderFactory.createTitledBorder(
 						Messages.getInstance().getString("VGLII.SpecificAllelicInteractions")));
 		interactionDetailsPanel.add(new UnknownSpecificsPanel());
 		masterPanel.add(interactionDetailsPanel);
 
 		this.add(masterPanel);
 	}
 	
 	public void setT1Value(String t1Value) {
 		this.t1Value = t1Value;
 	}
 
 	public void setT2Value(String t2Value) {
 		this.t2Value = t2Value;
 	}
 
 	public void setT3Value(String t3Value) {
 		this.t3Value = t3Value;
 	}
 
 	public void setT4Value(String t4Value) {
 		this.t4Value = t4Value;
 	}
 
 	public void setT5Value(String t5Value) {
 		this.t5Value = t5Value;
 	}
 
 	public void setT6Value(String t6Value) {
 		this.t6Value = t6Value;
 	}
 
 	public void clearValues() {
 		t1Value = "";
 		t2Value = "";
 		t3Value = "";
 		t4Value = "";
 		t5Value = "";
 		t6Value = "";
 	}
 
 	public void itemStateChanged(ItemEvent e) {
 
 		if (e.getSource().equals(alleleNumberChoices)) {
 			if (e.getItem().toString().equals(
 					Messages.getInstance().getString("VGLII.Unknown"))) {
 				interactionTypePanel.removeAll();
 				interactionTypePanel.add(new UnknownInteractionPanel());
 				interactionTypePanel.revalidate();
 
 				interactionDetailsPanel.removeAll();
 				interactionDetailsPanel.add(new UnknownSpecificsPanel());
 				interactionDetailsPanel.revalidate();
 				modelBuilderUI.pack();
 				modelBuilderUI.repaint();
 				clearValues();
 			}
 			if (e.getItem().toString().equals(
 					"2-" + Messages.getInstance().getString("VGLII.Allele"))) {
 				interactionTypePanel.removeAll();
 				TwoAllelePanel twap = new TwoAllelePanel(incDomPossible,
 						complementationPossible, epistasisPossible);
 				interactionTypeChoices = twap.getInteractionTypeChoices();
 				interactionTypeChoices.addItemListener(this);
 				interactionTypePanel.add(twap);
 				interactionTypePanel.revalidate();				
 
 				interactionDetailsPanel.removeAll();
 				interactionDetailsPanel.add(new UnknownSpecificsPanel());
 				interactionDetailsPanel.revalidate();
 				modelBuilderUI.pack();
 				modelBuilderUI.repaint();
 				clearValues();
 			}
 			if (e.getItem().toString().equals(
 					"3-" + Messages.getInstance().getString("VGLII.Allele"))) {
 				interactionTypePanel.removeAll();
 				ThreeAllelePanel thap = new ThreeAllelePanel(circularPossible);
 				interactionTypeChoices = thap.getInteractionTypeChoices();
 				interactionTypeChoices.addItemListener(this);
 				interactionTypePanel.add(thap);
 				interactionTypePanel.revalidate();				
 
 				interactionDetailsPanel.removeAll();
 				interactionDetailsPanel.add(new UnknownSpecificsPanel());
 				interactionDetailsPanel.revalidate();
 				modelBuilderUI.pack();
 				modelBuilderUI.repaint();
 				clearValues();
 			}
 
 		}
 
 		if (e.getSource().equals(interactionTypeChoices)) {
 
 			if (alleleNumberChoices.getSelectedItem().toString().equals(
 					"2-" + Messages.getInstance().getString("VGLII.Allele"))) {
 				if (e.getItem().toString().equals(
 						Messages.getInstance().getString("VGLII.Unknown"))) {
 					interactionDetailsPanel.setBorder(
 							BorderFactory.createTitledBorder(
 									Messages.getInstance().getString("VGLII.SpecificAllelicInteractions")));
 					interactionDetailsPanel.removeAll();
 					interactionDetailsPanel.add(new UnknownSpecificsPanel());
 					interactionDetailsPanel.revalidate();
 					modelBuilderUI.pack();
 					modelBuilderUI.repaint();
 					clearValues();
 				}
 				if (e.getItem().toString().equals(
 						Messages.getInstance().getString("VGLII.SimpleDominance"))) {
 					interactionDetailsPanel.setBorder(
 							BorderFactory.createTitledBorder(
 									Messages.getInstance().getString("VGLII.SpecificAllelicInteractions")));
 					interactionDetailsPanel.removeAll();
 					interactionDetailsPanel.add(
 							new TwoSimplePanel(traits, t1Choices, t2Choices, this));
 					interactionTypePanel.revalidate();
 					modelBuilderUI.pack();
 					modelBuilderUI.repaint();
 					clearValues();
 				}
 				if (e.getItem().toString().equals(
 						Messages.getInstance().getString("VGLII.IncompleteDominance"))) {
 					interactionDetailsPanel.setBorder(
 							BorderFactory.createTitledBorder(
 									Messages.getInstance().getString("VGLII.SpecificAllelicInteractions")));
 					interactionDetailsPanel.removeAll();
 					interactionDetailsPanel.add(
 							new TwoIncPanel(traits, t1Choices, t2Choices, t3Choices, this));
 					interactionTypePanel.revalidate();
 					modelBuilderUI.pack();
 					modelBuilderUI.repaint();
 					clearValues();
 				}
 				if (e.getItem().toString().equals(
 						Messages.getInstance().getString("VGLII.Complementation"))) {
 					interactionDetailsPanel.setBorder(
 							BorderFactory.createTitledBorder(
 									Messages.getInstance().getString("VGLII.Pathway")));
 					interactionDetailsPanel.removeAll();
 					interactionDetailsPanel.add(
 							new ComplementationPanel(traits, t1Choices, t2Choices, this));
 					interactionTypePanel.revalidate();
 					modelBuilderUI.pack();
 					modelBuilderUI.repaint();
 					clearValues();
 				}
 				if (e.getItem().toString().equals(
 						Messages.getInstance().getString("VGLII.Epistasis"))) {
 					interactionDetailsPanel.setBorder(
 							BorderFactory.createTitledBorder(
 									Messages.getInstance().getString("VGLII.Pathway")));
 					interactionDetailsPanel.removeAll();
 					interactionDetailsPanel.add(
 							new EpistasisPanel(traits, t1Choices, t2Choices, t3Choices, this));
 					interactionTypePanel.revalidate();
 					modelBuilderUI.pack();
 					modelBuilderUI.repaint();
 					clearValues();
 				}
 			}
 			if (alleleNumberChoices.getSelectedItem().toString().equals(
 					"3-" + Messages.getInstance().getString("VGLII.Allele"))) {
 				if (e.getItem().toString().equals(
 						Messages.getInstance().getString("VGLII.Unknown"))) {
 					interactionDetailsPanel.removeAll();
 					interactionDetailsPanel.add(new UnknownSpecificsPanel());
 					interactionDetailsPanel.revalidate();
 					modelBuilderUI.pack();
 					modelBuilderUI.repaint();
 					clearValues();
 				}
 				if (e.getItem().toString().equals(
 						Messages.getInstance().getString("VGLII.IncompleteDominance"))) {
 					interactionDetailsPanel.removeAll();
 					interactionDetailsPanel.add(
 							new ThreeIncPanel(
 									traits, 
 									t1Choices, 
 									t2Choices, 
 									t3Choices,
 									t4Choices,
 									t5Choices,
 									t6Choices, 
 									this));
 					interactionTypePanel.revalidate();		
 					modelBuilderUI.pack();
 					modelBuilderUI.repaint();
 					clearValues();
 				}
 				if (e.getItem().toString().equals(
 						Messages.getInstance().getString("VGLII.HierarchicalDominance"))) {
 					interactionDetailsPanel.removeAll();
 					interactionDetailsPanel.add(
 							new ThreeHierPanel(traits, 
 									t1Choices, t2Choices, t3Choices, this));
 					interactionTypePanel.revalidate();	
 					modelBuilderUI.pack();
 					modelBuilderUI.repaint();
 					clearValues();
 				}
 				if (e.getItem().toString().equals(
 						Messages.getInstance().getString("VGLII.CircularDominance"))) {
 					interactionDetailsPanel.removeAll();
 					interactionDetailsPanel.add(
 							new ThreeCircPanel(traits, 
 									t1Choices, t2Choices, t3Choices, this));
 					interactionTypePanel.revalidate();	
 					modelBuilderUI.pack();
 					modelBuilderUI.repaint();
 					clearValues();
 				}
 			}
 		}
 	}
 
 	public void setupActionListeners() {
 		if (alleleNumberChoices != null) {
 			alleleNumberChoices.addItemListener(this);	
 		}
 	}
 	
 	public void setStateFromFile(Element element) {
 		
 		List<Element> elements = element.getChildren();
 		Iterator<Element> it = elements.iterator();
 		while(it.hasNext()) {
 			Element e = it.next();
 			if (e.getName().equals("SexLinkage")) {
 				sexLinkageChoices.setSelectedItem((String)e.getText());
 			}
 			
 			if (e.getName().equals("AlleleNumber")) {
 				alleleNumberChoices.setSelectedItem((String)e.getText());
 			}
 			
 			if (e.getName().equals("InteractionType")) {
 				interactionTypeChoices.setSelectedItem((String)e.getText());
 			}
 
 			// get a reference to the details panel
 			ModelDetailsPanel mdp = (ModelDetailsPanel)interactionDetailsPanel.getComponents()[0];
 
 			if (e.getName().equals("T1")) {
 				setT1Value((String)e.getText());
 				mdp.updateT1Choices((String)e.getText());
 			}
 			
 			if (e.getName().equals("T2")) {
 				setT2Value((String)e.getText());
 				mdp.updateT2Choices((String)e.getText());
 			}
 			
 			if (e.getName().equals("T3")) {
 				setT3Value((String)e.getText());
 				mdp.updateT3Choices((String)e.getText());
 			}
 			
 			if (e.getName().equals("T4")) {
 				setT4Value((String)e.getText());
 				mdp.updateT4Choices((String)e.getText());
 			}
 			
 			if (e.getName().equals("T5")) {
 				setT5Value((String)e.getText());
 				mdp.updateT5Choices((String)e.getText());
 			}
 			
 			if (e.getName().equals("T6")) {
 				setT6Value((String)e.getText());
 				mdp.updateT6Choices((String)e.getText());
 			}
 		}
 	}
 		
 	public Element save() {
 		Element mpe = new Element("Character");
 		mpe.setAttribute("Name", character);
 		mpe.setAttribute("Index", String.valueOf(index));
 		
 		Element e = null;
 		e = new Element("SexLinkage");
 		e.setText(String.valueOf(sexLinkageChoices.getSelectedItem()));
 		mpe.addContent(e);
 		
 		e = new Element("AlleleNumber");
 		e.setText(String.valueOf(alleleNumberChoices.getSelectedItem()));
 		mpe.addContent(e);
 		
 		e = new Element("InteractionType");
 		e.setText(String.valueOf(interactionTypeChoices.getSelectedItem()));
 		mpe.addContent(e);
 
 		e = new Element("T1");
 		e.addContent(t1Value);
 		mpe.addContent(e);
 
 		e = new Element("T2");
 		e.addContent(t2Value);
 		mpe.addContent(e);
 
 		e = new Element("T3");
 		e.addContent(t3Value);
 		mpe.addContent(e);
 
 		e = new Element("T4");
 		e.addContent(t4Value);
 		mpe.addContent(e);
 
 		e = new Element("T5");
 		e.addContent(t5Value);
 		mpe.addContent(e);
 
 		e = new Element("T6");
 		e.addContent(t6Value);
 		mpe.addContent(e);
 
 		return mpe;
 	}
 	
 	public String getAsHtml() {
 		StringBuffer b = new StringBuffer();
 		b.append("<b>" + character + "</b><br>");
 		b.append("<ul>");
 		b.append("<li>" + alleleNumberChoices.getSelectedItem() + "</li>");
 		b.append("<li>" + interactionTypeChoices.getSelectedItem() + "</li>");
 		ModelDetailsPanel mdp = (ModelDetailsPanel)interactionDetailsPanel.getComponents()[0];
 		b.append(mdp.getAsHtml());
 		b.append("</ul><br>");
 		return b.toString();
 	}
 }
