 /*
  * Copyright 2012 Frédéric SACHOT
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package kesako.hmi.meta;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.InputVerifier;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import kesako.common.Meta;
 import kesako.utilities.DBUtilities;
 import kesako.utilities.FileUtilities;
 import kesako.utilities.XMLUtilities;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 
 public class AddMetaPanel extends JPanel{
 	private static final long serialVersionUID = -8668407800902178469L;
 	private static final Logger logger = Logger.getLogger(AddMetaPanel.class);
 	/**
 	 * Root node of the document that list meta-data
 	 */
 	private Node rootMetaXML;
 	/**
 	 * Vector of meta-data
 	 */
 	private Vector<Meta> vMetas;
 	/**
 	 * Vector of panels representing the different meta-data
 	 */
 	private TreeMap<String,MetaPanel> vMetaPanels;
 	/**
 	 * Panel showing the list of meta-data. This panel is composed by MetaPanel objects stored in vMetaPanels vector.
 	 */
 	private JPanel jpMeta;
 	/**
 	 * Text representing the selected files
 	 */
 	private JTextArea labSourcePath;
 	/**
 	 * Save button
 	 */
 	private JButton bSave;
 	/**
 	 * Choose file button
 	 */
 	private JPanel jpChooseFile;
 	private File initialDirectory;
 
 
 
 	private JPanel jpPath;
 	private JList<String> listValues;
 	private Vector<String> metaValues;
 	private MetaPanel selectedMetaPanel;
 
 	public AddMetaPanel (){
 		logger.debug("Construction AddMetaPanel");
 		this.setLayout(new BorderLayout());
 		this.initialDirectory=null;
 
 		try {
 			Document doc=XMLUtilities.getXMLDocument("meta.xml");
 			rootMetaXML=doc.getDocumentElement();
 
 			labSourcePath=new JTextArea();
 			//labSourcePath.setWrapStyleWord(true);
 			labSourcePath.setLineWrap(true);
 			jpPath=new JPanel();
 			jpPath.setLayout(new BoxLayout(jpPath, BoxLayout.X_AXIS));
 			jpPath.setBorder(new TitledBorder("Selected Files"));
 			jpPath.add(labSourcePath);
 
 			metaValues = new Vector<String>();
 			listValues = new JList<String>(new FileListMetaModel(metaValues));
 			listValues.setBorder(new TitledBorder("Proposed value"));
 			listValues.addMouseListener(new MouseListener(){
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					@SuppressWarnings("unchecked")
 					JList<String> jL=(JList<String>)e.getSource();
 					String temp = selectedMetaPanel.getMetaValue().trim();
 					if(!temp.toLowerCase().contains(jL.getSelectedValue().toLowerCase())){
 						if(temp.equals("")){
 							selectedMetaPanel.setMetaValue(jL.getSelectedValue());
 						}else{
 							selectedMetaPanel.setMetaValue(temp+", "+jL.getSelectedValue());
 						}
 					}
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent arg0) {
 				}
 				@Override
 				public void mouseExited(MouseEvent arg0) {
 				}
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 				}
 				@Override
 				public void mouseReleased(MouseEvent arg0) {
 				}				
 			});
 
 			GridBagConstraints cMeta=new GridBagConstraints();
 			cMeta.insets=new Insets(2,5,2,5);
 			cMeta.ipadx=5;
 			cMeta.ipady=5;
 			cMeta.fill=GridBagConstraints.BOTH;
 			cMeta.gridheight=1;
 			cMeta.gridwidth=1;
 			cMeta.weightx=1;
 			cMeta.weighty=0;
 
 			//Meta list construction
 			jpMeta = new JPanel();
 			jpMeta.setLayout(new GridBagLayout());
 			jpMeta.setBorder(new TitledBorder("Meta list : "));
 			NodeList listMetaXML = ((Element)rootMetaXML).getElementsByTagName("meta");
 			Node item;
 			Meta meta;
 			MetaPanel mp;
 			vMetaPanels=new TreeMap<String,MetaPanel>();
 			vMetas=new Vector<Meta>();
 			cMeta.gridx=0;
 			int nbMeta;
 			for(nbMeta=0;nbMeta<listMetaXML.getLength();nbMeta++){
 				cMeta.gridy=nbMeta;
 				item=listMetaXML.item(nbMeta);
 				meta=new Meta(item);
 				if(meta.isVisibleInMetaPanel()){
 					mp=new MetaPanel(meta,this,nbMeta%2);
 					if(meta.getName().trim().equalsIgnoreCase("date")){
 						mp.setInputVerifier(new DateVerifier());
 						mp.setDefaultValue("yyyy-mm-dd");
 					}
 					vMetaPanels.put(meta.getName(), mp);
 					vMetas.add(meta);
 					jpMeta.add(mp,cMeta);
 				}
 			}
 			cMeta.gridx=1;
 			cMeta.gridy=0;
 			//cMeta.weightx=0;
 			cMeta.weighty=0;
 			cMeta.gridheight=nbMeta+2;
 			jpMeta.add(listValues,cMeta);
 
 			cMeta.gridx=0;
 			cMeta.gridy=nbMeta;
 			cMeta.fill=GridBagConstraints.NONE;
 			cMeta.weightx=0;
 			cMeta.gridheight=1;
 			cMeta.gridwidth=1;			
 			bSave = new JButton("Save");
 			jpMeta.add(bSave,cMeta);
 			bSave.addActionListener(new ActionListener() {		
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					String[] fileName=labSourcePath.getText().split("\\|");
 					for(int i=0; i<fileName.length;i++){
 						logger.debug(i+" : "+fileName[i]);
 						File f=new File(fileName[i].trim());
 						if(f.exists()){
 							saveMetaXML(FileUtilities.getFileMetaName(fileName[i].trim()));
 						}
 					}
 					drawInit();
 				}
 			});
 
 			cMeta.gridy=nbMeta+1;
 			cMeta.weighty=1;
 			cMeta.fill=GridBagConstraints.BOTH;
 			jpMeta.add(new JPanel(),cMeta);
 
 			JButton bChooseFile = new JButton("Choose File");
 			jpChooseFile=new JPanel();
 			jpChooseFile.add(bChooseFile);
 			bChooseFile.addActionListener(new ActionListener() {			
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					JFileChooser chooser = new JFileChooser();
 					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 					chooser.setMultiSelectionEnabled(true);
 					chooser.setAcceptAllFileFilterUsed(false);
 					chooser.addChoosableFileFilter(new FileFilter(){
 						@Override
 						public boolean accept(File f) {
 							boolean test;
 							test=DBUtilities.isAccetableFile(f);
 							if(!f.isDirectory()){
 								test=test&&FileUtilities.isValidExtansion(f);
 							}
 							return test;
 						}
 						@Override
 						public String getDescription() {
 							return "Indexable File";
 						}
 					});
 					chooser.setCurrentDirectory(initialDirectory);
 					int returnVal = chooser.showOpenDialog((Component)e.getSource());
 					if(returnVal == JFileChooser.APPROVE_OPTION) {
 						String paths="";
 						initialDirectory=chooser.getCurrentDirectory();
 						for(int i=0;i<chooser.getSelectedFiles().length;i++){
 							logger.debug("Sélection fichier : "+chooser.getSelectedFiles()[i].getAbsolutePath());
 							if(i>0){
 								paths+=" | ";
 							}
 							paths+=chooser.getSelectedFiles()[i].getAbsolutePath();
 							initMeta(chooser.getSelectedFiles()[i].getAbsolutePath());
 						}
 
 						removeAll();
 						add(jpPath,BorderLayout.NORTH);
 						labSourcePath.setText(paths);
 						add(jpMeta,BorderLayout.CENTER);
						//paintAll(getGraphics());
 					}			
 				}	
 			});
 		} catch (ParserConfigurationException e1) {
 			logger.fatal("erreur XML :",e1);
 		} catch (SAXException e1) {
 			logger.fatal("erreur XML :",e1);
 		} catch (IOException e1) {
 			logger.fatal("erreur XML :",e1);
 		}
 	}
 
 	public void drawInit(){
 		//définition de l'interface
 		logger.debug("Draw Init");
 		this.removeAll();
 		this.add(jpChooseFile,BorderLayout.NORTH);
 		this.paintAll(this.getGraphics());
 	}
 	/**
 	 * Initiating values of meta-data. If multiple files are selected, meta-data are initiated by the first value of the meta-data. 
 	 * @param nomFile
 	 */
 	private void initMeta(String nomFile){
 		File fileMeta;
 		try {
 			String nomFicMeta=FileUtilities.getFileMetaName(nomFile);
 			String nomMeta,valueMeta;
 			logger.debug("nomFicMeta="+nomFicMeta);
 			fileMeta=new File(nomFicMeta);
 			if(fileMeta.exists() && fileMeta.isFile()){
 				Document doc = XMLUtilities.getXMLDocument(nomFicMeta);
 				Node root=doc.getDocumentElement();
 				logger.debug("Root element " + root.getNodeName());
 				NodeList nodeLst = root.getChildNodes();
 				for (int s = 0; s < nodeLst.getLength(); s++) {
 					Node fstNode = nodeLst.item(s);
 					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
 						logger.debug(fstNode.getNodeName()+" : "+fstNode.getAttributes().item(0).getNodeValue()+" : "+fstNode.getAttributes().item(1).getNodeValue());
 						if(fstNode.getNodeName().equalsIgnoreCase("meta")){
 							nomMeta="";
 							valueMeta="";
 							for(int j=0;j<fstNode.getAttributes().getLength();j++){
 								if(fstNode.getAttributes().item(j).getNodeName().equalsIgnoreCase("name")){
 									nomMeta=fstNode.getAttributes().item(j).getNodeValue();														
 								}
 								if(fstNode.getAttributes().item(j).getNodeName().equalsIgnoreCase("value")){
 									valueMeta=fstNode.getAttributes().item(j).getNodeValue();														
 								}
 							}
 							logger.debug("nomMeta : "+nomMeta+", value="+valueMeta);
 							if(vMetaPanels.containsKey(nomMeta)){
 								if(!valueMeta.trim().equals("")){
 									vMetaPanels.get(nomMeta).setMetaValue(valueMeta);
 								}else{
 									vMetaPanels.get(nomMeta).setMetaValue(vMetaPanels.get(nomMeta).getDefaultValue());
 								}
 							}
 						}
 					}
 				}	
 			}
 		} catch (SAXException e) {
 			logger.fatal("file processing : "+e);
 		} catch (IOException e) {
 			logger.fatal("file processing : "+e);
 		} catch (ParserConfigurationException e) {
 			logger.fatal("file processing : "+e);
 		}
 		for(String key:vMetaPanels.keySet()){
 			vMetaPanels.get(key).updateValues();
 		}
 	}
 
 	private void saveMetaXML(String nomFileMeta){
 		//création du fichier xml
 		logger.debug("Création fichier de sources XML");
 		DocumentBuilderFactory factoryXML;
 		DocumentBuilder builderXML;
 		DOMImplementation implDOM;
 		Document docXML;
 		Element rootXML,metaXML;
 		factoryXML = DocumentBuilderFactory.newInstance();
 		try {
 			builderXML = factoryXML.newDocumentBuilder();
 			implDOM = builderXML.getDOMImplementation();
 			docXML = implDOM.createDocument(null,null,null);
 			rootXML=docXML.createElement("root");
 			docXML.appendChild(rootXML);
 			for(int i=0;i<vMetas.size();i++){
 				metaXML=docXML.createElement("meta");
 				metaXML.setAttribute("name", vMetas.get(i).getName());
 				metaXML.setAttribute("value",vMetaPanels.get(vMetas.get(i).getName()).getMetaValue());
 				rootXML.appendChild(metaXML);
 			}			
 			XMLUtilities.saveFileXML(docXML,nomFileMeta);
 		} catch (ParserConfigurationException e) {
 			logger.fatal("initXML ",e);
 		} catch (TransformerConfigurationException e) {
 			logger.fatal("initXML ",e);
 		} catch (TransformerException e) {
 			logger.fatal("initXML ",e);
 		} catch (DOMException e) {
 			logger.fatal("initXML ",e);
 		} catch (IOException e) {
 			logger.fatal("initXML ",e);
 		}
 		logger.debug("Fin création fichier de sourcesXML");
 
 	}
 
 	public JList<String> getListValues() {
 		return listValues;
 	}
 
 	public Vector<String> getMetaValues() {
 		return metaValues;
 	}
 
 	public void setSelectedMetaPanel(MetaPanel selectedMetaPanel) {
 		this.selectedMetaPanel = selectedMetaPanel;
 	}
 	
 	private class DateVerifier extends InputVerifier{
 		@Override
 		public boolean verify(JComponent input) {
 			JTextField tf = (JTextField) input;
 			String dateTxt=tf.getText().trim();
 			boolean valid=dateTxt.matches("\\d{4}-\\d{2}-\\d{2}");
 			if(!valid && !dateTxt.trim().equals("") && !dateTxt.trim().equalsIgnoreCase("yyyy-mm-dd")){
 				JOptionPane.showMessageDialog(input, "The date is in the wrong format.\n The format is yyyy-mm-dd","Date Format Error", JOptionPane.ERROR_MESSAGE);
 			}else{
 				valid=true;
 			}
 			return valid;
 		}
 		
 	}
 }
