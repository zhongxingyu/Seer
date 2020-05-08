 package models.transform;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import models.domain.DomainAttribute;
 import models.domain.DomainClass;
 import models.domain.DomainDiagram;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class TransformER_Domain {
 	DocumentBuilderFactory factoryBuilder;
 	DocumentBuilder documentBuilder;
 	
 	private Document diagramDoc; // Antiguo modelo Entidad Relacion
 	private Document graphDoc;	// Antiguo xml de la representacion gr�fica 
 	private Document dominioDoc; // Modelo de dominio generado
 	private Document newGraphDoc;// xml de la nueva representacion gr�fica
 	private int newRelationshipNumeber;
 	
 	public TransformER_Domain(Document diagram, Document graph){
 		this.newRelationshipNumeber = 0;
 		this.factoryBuilder = DocumentBuilderFactory.newInstance();
 		this.factoryBuilder.setNamespaceAware(true);
 		try {
 			this.documentBuilder = this.factoryBuilder.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		this.diagramDoc=diagram;
 		this.graphDoc=graph;
 		this.dominioDoc = null;
 		this.newGraphDoc = null;
 	}
 	
 	
 	public Document GetDomainModel(){
         // Diagram element
         Element diagram=this.diagramDoc.getDocumentElement();
 		
 	    // Creamos el Document para el modelo de dominio
 		dominioDoc = this.documentBuilder.newDocument();
         Element eDominio = dominioDoc.createElement("dominio");
         eDominio.setAttribute("id", diagram.getAttribute("id"));
         dominioDoc.appendChild(eDominio);
         
         Element eClases = dominioDoc.createElement("classes");
         eDominio.appendChild(eClases);
         
         Element eRelations = dominioDoc.createElement("relationships");
         eDominio.appendChild(eRelations);
         
         Element eSubdiagrams = dominioDoc.createElement("subdiagrams");
         eDominio.appendChild(eSubdiagrams);
         
         // Lista de entities
         Element entitiesElement = (Element) diagram.getElementsByTagName("entities").item(0);
         this.procesarEntidades(entitiesElement);
         
         // Lista de relaciones
         Element relationshipElement = (Element) diagram.getElementsByTagName("relationships").item(0);
         this.procesarRelaciones(relationshipElement);
         
         // Lista de subdiagramas
         Element subDiagramsElement = (Element) diagram.getElementsByTagName("subDiagrams").item(0);
         this.procesarSubDiagramas(subDiagramsElement, eSubdiagrams);
         
         
         return dominioDoc;
 	}
 	
 	private void procesarSubDiagramas(Element subDiagramsElement, Element target) {
 		NodeList elements = subDiagramsElement.getElementsByTagName("diagram");
         for (int i = 0; i < elements.getLength(); i++) {
         	Element eDiagram = (Element) elements.item(i);
 			Element subDiagram = dominioDoc.createElement("domainDiagram");
 			subDiagram.setAttribute("name", eDiagram.getAttribute("name"));
 			subDiagram.setAttribute("id", eDiagram.getAttribute("id"));
         	target.appendChild(subDiagram);
         }
 	}
 
 
 	private void procesarRelaciones(Element relationshipsElement){
         Element eClases= (Element)dominioDoc.getElementsByTagName("classes").item(0);
         Element eRelationsDomain= (Element)dominioDoc.getElementsByTagName("relationships").item(0);
         
         NodeList DERRelationshipList = relationshipsElement.getElementsByTagName("relationship");
         
         for (int temp = 0; temp < DERRelationshipList.getLength(); temp++) {
  		   Node nNode = DERRelationshipList.item(temp);
  		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 				  Element relacionDER= (Element) nNode;
 				  
 				  String sId=relacionDER.getAttribute("id");
 				  String sName=relacionDER.getAttribute("name");
 				 			
 				  Element eAtributos=(Element)relacionDER.getElementsByTagName("attributes").item(0);
 				  NodeList attrList=eAtributos.getChildNodes();
 				  
 				  //si tiene atributos la relacion mapea a clase
 				  if(attrList.getLength()!=0){
 					  System.out.println("Relationship name: " + sName);
 
 					  Element relationshipClass = dominioDoc.createElement("class");
 					  relationshipClass.setAttribute("name", sName);
 					  relationshipClass.setAttribute("id", sId);
 					  eClases.appendChild(relationshipClass);
 
 					  // Procesar nodos hijos
 
 					  this.procesarAtributos(relationshipClass, eAtributos);
 					  procesarRelacionConAtributos(relacionDER,eRelationsDomain,relationshipClass);
 					  
 				  }
 				  else
 					  procesarRelacionSinAtributos(relacionDER,eRelationsDomain);
 					   
 				  System.out.println("Fin Relationship: " + sName);
  		   }
  		}        
 	}
 	
 	private void procesarRelacionConAtributos(Element relacionDER, Element eRelationsDomain,
 			Element relationshipClass) {
 
 		String sComposition = relacionDER.getAttribute("composition");
 		  
 		  if(sComposition == "true"){
 			//creo una relacion normal con 2 clases
 			  procesarRelacionSinAtributos(relacionDER,eRelationsDomain); 
 			  
 		  }
 		  else{
 			  //creo 2 relaciones contra la relacion que se hizo clase
 			  crearRelacionDoble( relacionDER, eRelationsDomain, relationshipClass);
 		  }
 
 	}
 
 
 	private void crearRelacionDoble(Element relacionDER, Element eRelationsDomain,
 			Element relacionHechaClase) {
 		
 		Element eEntities=(Element)relacionDER.getElementsByTagName("entities").item(0);
 		Element nuevaRelacionHechaClaseIzq = dominioDoc.createElement("class");
 		Element nuevaRelacionHechaClaseDer = dominioDoc.createElement("class");
 		
 		nuevaRelacionHechaClaseIzq.setAttribute("id", relacionHechaClase.getAttribute("id"));
 		nuevaRelacionHechaClaseDer.setAttribute("id", relacionHechaClase.getAttribute("id"));
 				
 
 		Element eRelationIzq = dominioDoc.createElement("relationship");
 		String sId1=relacionDER.getAttribute("id"); //TODO: NO SE PUEDE REPETIR LE ID DE LA RELACION DEL DER EN LAS 2 RELACIONES DEL MODELO DE DOMINIO
 		String sName1=relacionDER.getAttribute("name");
 		String sComposition = relacionDER.getAttribute("composition");
 		
 		eRelationIzq.setAttribute("id", sId1);
 		eRelationIzq.setAttribute("name", sName1 + "Izq");
 		eRelationIzq.setAttribute("composition", sComposition);
 		eRelationIzq.setAttribute("directionality", "bidirectional");
 		eRelationsDomain.appendChild(eRelationIzq);
 
 		Element eRelationDer = dominioDoc.createElement("relationship");
 		eRelationDer.setAttribute("id", sId1);
 		eRelationDer.setAttribute("name", sName1 + "Der");
 		eRelationDer.setAttribute("composition", sComposition);
 		eRelationDer.setAttribute("directionality", "bidirectional");
 		eRelationsDomain.appendChild(eRelationDer);
 		
 		Element eClasesIzq = dominioDoc.createElement("classes");
 		Element eClasesDer = dominioDoc.createElement("classes");
 		
 		NodeList entitiyList=eEntities.getChildNodes(); 
 		String sCardinality[] = new String[entitiyList.getLength()];
         System.out.println("Numero de clases: " + entitiyList.getLength());
         List<Element> elementsList = new ArrayList<Element>() ;
         
         for (int temp = 0; temp < entitiyList.getLength(); temp++) {
  		   Node nNode = entitiyList.item(temp);
  		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 				  Element eEntity= (Element) nNode;
 				  
 				  String sId=eEntity.getAttribute("entityId");
 				  sCardinality[temp] = eEntity.getAttribute("maximumCardinality");
 				  
 				  System.out.println("class ID: " + sId);
 	      
 				  Element eClase = dominioDoc.createElement("class");
 				  eClase.setAttribute("id", sId);
 
 				  elementsList.add(eClase);
 				  
  		   }
  		}     
         		
         elementsList.get(0).setAttribute("cardinality", "1");
 		eClasesIzq.appendChild(elementsList.get(0));
 		nuevaRelacionHechaClaseIzq.setAttribute("cardinality", sCardinality[1]);
 		eClasesIzq.appendChild(nuevaRelacionHechaClaseIzq);
 		eRelationIzq.appendChild(eClasesIzq);
 		
 		elementsList.get(1).setAttribute("cardinality", "1");
 		eClasesDer.appendChild(elementsList.get(1));
 		nuevaRelacionHechaClaseDer.setAttribute("cardinality", sCardinality[0]);
 		eClasesDer.appendChild(nuevaRelacionHechaClaseDer);
 		eRelationDer.appendChild(eClasesDer);
 	}
 
 
 	private void procesarRelacionSinAtributos(Element relacionDER, Element eRelations) {
 
 		Element eEntities=(Element)relacionDER.getElementsByTagName("entities").item(0);
 		
 
 		Element eRelation = dominioDoc.createElement("relationship");
 		String sId1 = relacionDER.getAttribute("id");
 		String sName1 = relacionDER.getAttribute("name");
 		String sComposition = relacionDER.getAttribute("composition");
 		eRelation.setAttribute("id", sId1);
 		eRelation.setAttribute("name", sName1);
 		eRelation.setAttribute("composition", sComposition);
 		eRelation.setAttribute("directionality", "bidirectional");
 		eRelations.appendChild(eRelation);  
 		
 		Element eClases = dominioDoc.createElement("classes");
 		NodeList entityList=eEntities.getChildNodes(); 
 		String sCardinality[] = new String[entityList.getLength()];
         System.out.println("Numero de clases: " + entityList.getLength());
         List<Element> elementsList = new ArrayList<Element>() ;
         
         for (int temp = 0; temp < entityList.getLength(); temp++) {
  		   Node nNode = entityList.item(temp);
  		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 				  Element eEntity= (Element) nNode;
 				  
 				  String sId=eEntity.getAttribute("entityId");
 				  sCardinality[temp] = eEntity.getAttribute("maximumCardinality");
 				  
 				  System.out.println("class ID: " + sId);
 	      
 				  Element eClase = dominioDoc.createElement("class");
 				  eClase.setAttribute("id", sId);
 
 				  elementsList.add(eClase);
 				  
  		   }
  		}
         
         int i=1;
 		for(Element eClase:elementsList){
 			eClase.setAttribute("cardinality", sCardinality[i]);
 			i--;
 			eClases.appendChild(eClase);
 		}
         eRelation.appendChild(eClases);
 	}
 
 	private void procesarEntidades(Element entitiesElement){
         Element eClases= (Element)dominioDoc.getElementsByTagName("classes").item(0);
         
         NodeList nList = entitiesElement.getElementsByTagName("entity");
         
         for (int temp = 0; temp < nList.getLength(); temp++) {
  		   Node nNode = nList.item(temp);
  		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 				  Element eEntity= (Element) nNode;
 				  
 				  String sId=eEntity.getAttribute("id");
 				  String sName=eEntity.getAttribute("name");
 				  
 				  System.out.println("Entity name: " + sName);
 	      
 				  Element clase = dominioDoc.createElement("class");
 				  clase.setAttribute("name", sName);
 				  clase.setAttribute("id", sId);
 				  eClases.appendChild(clase);
 				  
 				  // Procesar nodos hijos
 				  Element eAtributos=(Element)eEntity.getElementsByTagName("attributes").item(0);
 				  this.procesarAtributos(clase, eAtributos);
 				  
 				  System.out.println("Fin Entity: " + sName);
  		   }
  		}        
 	}
 	
 	private void procesarAtributos(Element clase,Element eAtributes){
 		Element eAtributos = dominioDoc.createElement("attributes");
 		clase.appendChild(eAtributos);
 		
         //NodeList nList = eAtributes.getElementsByTagName("attribute");
 		NodeList nList=eAtributes.getChildNodes(); 
         
         System.out.println("Numero de atributos: " + nList.getLength());
         
         for (int temp = 0; temp < nList.getLength(); temp++) {
  		   Node nNode = nList.item(temp);
  		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 				  Element eAtribute= (Element) nNode;
 				  
 				  String sId=eAtribute.getAttribute("id");
 				  String sName=eAtribute.getAttribute("name");
 				  String sMinCardinality = eAtribute.getAttribute("minimumCardinality");
 				  String sMaxCardinality = eAtribute.getAttribute("maximumCardinality");
 				  
 				  System.out.println("Attribute name: " + sName);
 	      
 				  Element eAtributo = dominioDoc.createElement("attribute");
 				  eAtributo.setAttribute("name", sName);
 				  eAtributo.setAttribute("id", sId);
 				  eAtributo.setAttribute("minCardinality", sMinCardinality);
 				  eAtributo.setAttribute("maxCardinality", sMaxCardinality);
 				  
 				  // Procesar nodos hijos
 				  Element eAtributosHijos=(Element) eAtribute.getElementsByTagName("attributes").item(0);
 				  NodeList nListaHijos = eAtributosHijos.getElementsByTagName("attribute");				  
 				  if (nListaHijos.getLength()>0){
 						Element clases = (Element)dominioDoc.getElementsByTagName("classes").item(0);
 				        Element attributoHechoClase= dominioDoc.createElement("class");
 				        attributoHechoClase.setAttribute("name", sName);
 				        attributoHechoClase.setAttribute("id", sId);
 				        clases.appendChild(attributoHechoClase);
 				        
 				        //crearRelacion(crearClaseDeRelacion(clase,"1.0"),crearClaseDeRelacion(attributoHechoClase,sCardinality));
 				        crearRelacion(clase,attributoHechoClase);
 				        
 				        this.procesarAtributos(attributoHechoClase,eAtributosHijos );  
 				  }else
 					  eAtributos.appendChild(eAtributo);
  		   }
  		}        
  	}
 	
 	private void crearRelacion(Element claseContenedora, Element clase) {
 		Element eRelationsDomain= (Element)dominioDoc.getElementsByTagName("relationships").item(0);
 
 		Element eRelation = translateRelationship(claseContenedora,clase);
 		eRelationsDomain.appendChild(eRelation);  
 		
 		Element eClases = dominioDoc.createElement("classes");
 		Element eClass1= dominioDoc.createElement("class");
 		Element eClass2= dominioDoc.createElement("class");
 		eClass1.setAttribute("id", claseContenedora.getAttribute("id"));
 		eClass1.setAttribute("cardinality", "1");
 		eClass2.setAttribute("id", clase.getAttribute("id"));
 		eClass2.setAttribute("cardinality", "1");
 		
 		eClases.appendChild(eClass1);
 		eClases.appendChild(eClass2);
         
         eRelation.appendChild(eClases);
         eRelationsDomain.appendChild(eRelation);
 	}
 
 	private Element translateRelationship(Element claseContenedora,Element clase) {
 		Element eRelation = dominioDoc.createElement("relationship");
 		String sId1 = claseContenedora.getAttribute("id");
 		String sName1 = claseContenedora.getAttribute("name");
 		String sComposition1 = claseContenedora.getAttribute("composition");
 		//nueva relacion, generar nuevo id
 		//eRelation.setAttribute("id", null);
 		eRelation.setAttribute("id", "Relacion" + sId1);
 		//eRelation.setAttribute("name", "atributo" + this.newRelationshipNumeber++);
 		eRelation.setAttribute("name", "atrb_" + clase.getAttribute("name"));
 		eRelation.setAttribute("composition", "false");
 		eRelation.setAttribute("directionality", "bidirectional");
 		return eRelation;
 	}
 
 	public Document getGraphDomain(DomainDiagram domainDiagram){
 		this.newGraphDoc = this.documentBuilder.newDocument();
 
 		System.out.println("Se ejecuta getGraphDomain");
 		  
 		Element eGraphModel = this.newGraphDoc.createElement("mxGraphModel");
 		this.newGraphDoc .appendChild(eGraphModel);
         
         Element eRoot = this.newGraphDoc.createElement("root");
         eGraphModel.appendChild(eRoot);
         
         Element eCeldaCero = this.newGraphDoc.createElement("mxCell");
         eCeldaCero.setAttribute("id","0");
         eRoot.appendChild(eCeldaCero);
 
         Element eCeldaPadre = this.newGraphDoc.createElement("mxCell");
         eCeldaPadre.setAttribute("id","1");        
         eCeldaPadre.setAttribute("parent","0");        
         eRoot.appendChild(eCeldaPadre);
 
         List<DomainClass> ListC= domainDiagram.getDomainClasses();
         Iterator<DomainClass> iterator = ListC.iterator();
     	while (iterator.hasNext()) {
     		String x="0";
     		String y="0";
     		
     		DomainClass dClass= iterator.next();
     		System.out.println("Procesamos Clase: " + dClass.getName());
     		
     		//Collection<DomainAttribute> dAttrib=dClass.getAttributes();
 
             Element eGeometia=getElementGeometrybyID(dClass.getSID(),dClass.getName());
             if (eGeometia!=null){
             	x=eGeometia.getAttribute("x");
             	y=eGeometia.getAttribute("y");
             }else{
         		System.out.println("Geometria no encontrada, sid: " + dClass.getSID());            	
             }
     		
             Element eCelda = this.newGraphDoc.createElement("mxCell");
             eCelda.setAttribute("id",dClass.getSID());
             eCelda.setAttribute("parent","1");
             eCelda.setAttribute("style","verticalAlign=top;fontSize=12;fontColor=#000000;fillColor=#FFFFB6;strokeColor=black");
             eCelda.setAttribute("value",dClass.getName());
             eCelda.setAttribute("vertex","1");
 
             System.out.println("Constante: " + com.mxgraph.util.mxConstants.FONT_BOLD);
             
             Element eGeometry= this.newGraphDoc.createElement("mxGeometry");
             eGeometry.setAttribute("as","geometry");
             eGeometry.setAttribute("height","140.0");
             eGeometry.setAttribute("width","110.0");
             eGeometry.setAttribute("x",x);
             eGeometry.setAttribute("y",y);
             
             eCelda.appendChild(eGeometry);
             
             eRoot.appendChild(eCelda);
 
             // Agregar celdas de los atributos
             addLineCell( eRoot,dClass, 20);
             int altura=this.addCellAttributes(eRoot, dClass);
             addLineCell( eRoot,dClass,altura+20);
     	}        
     	
     	System.out.println("Buscamos las relacioens."); 	
         Element eRelaciones= (Element)dominioDoc.getElementsByTagName("relationships").item(0);
         NodeList nListR = eRelaciones.getElementsByTagName("relationship");
         for (int temp = 0; temp < nListR.getLength(); temp++) {
   		   Node nNode = nListR.item(temp);
   		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
   				  Element eRelationship= (Element) nNode;
   				  String comp=eRelationship.getAttribute("composition");
   			   	  System.out.println("First class: "+eRelationship.getAttribute("name"));
   			   	  String sName=eRelationship.getAttribute("name");
   			   	  String relId=eRelationship.getAttribute("id");
   				  if (comp.compareTo("false")==0){
   					  Element eClasses =(Element)eRelationship.getFirstChild();
   					  
   					  Element firstC=(Element)eClasses.getFirstChild();
   					  Element lastC=(Element)eClasses.getLastChild();
   					  
   					  String class1id=firstC.getAttribute("id");
   					  String class2id=lastC.getAttribute("id");
   					  System.out.println("First class: "+class1id);
   					  System.out.println("Last class: "+class2id);
   					  
 					  Element eRel = this.newGraphDoc.createElement("mxCell");
 					  eRel.setAttribute("edge","1");
 					  eRel.setAttribute("id","Relacion"+relId+sName);
 					  eRel.setAttribute("parent","1");
					  eRel.setAttribute("style","endArrow=none;edgeStyle=elbowEdgeStyle;verticalAlign=bottom;align=left;strokeColor=#000000");
 					  eRel.setAttribute("value",sName);					  
 					  eRel.setAttribute("source",class1id);
 					  eRel.setAttribute("target",class2id);
 						
 					  Element eGeom= this.newGraphDoc.createElement("mxGeometry");
 					  eGeom.setAttribute("as","geometry");
 					  eGeom.setAttribute("relative","1");
 					  eRel.appendChild(eGeom);
 						
 					  eRoot.appendChild(eRel);
   				  }else{
   					  
   					  
   				  }
   		   }
         }
 
     	System.out.println("Buscamos las herencias."); 	
         Element eHierarchies = (Element)diagramDoc.getElementsByTagName("hierarchies").item(0);
 
         connectHierarchies(eHierarchies, eRoot);
 
 		return this.newGraphDoc ;
 	}
 
 	private void connectHierarchies(Element eHierarchies, Element eRoot) {
 
 		NodeList hierarchies = eHierarchies.getElementsByTagName("hierarchy");
 
 		for (int i = 0; i < hierarchies.getLength(); i ++) {
 			
 			Element currentHierarchy = (Element) hierarchies.item(i);
 			String parentClassId = currentHierarchy.getAttribute("generalEntityId");
 			String hierarchyId = currentHierarchy.getAttribute("id");
 			NodeList subClasses = currentHierarchy.getLastChild().getChildNodes();
 
 			for (int j = 0; j < subClasses.getLength(); j ++) {
 				Node currentSubClass = subClasses.item(j);
 				String subClassId = currentSubClass.getTextContent();
 
 				Element eRel = newGraphDoc.createElement("mxCell");
 				eRel.setAttribute("edge","1");
 				eRel.setAttribute("id","Hierarchy-" + hierarchyId);
 				eRel.setAttribute("parent","1");
 				eRel.setAttribute("style","endArrow=block;edgeStyle=elbowEdgeStyle;verticalAlign=bottom;align=left;strokeColor=#000000");
 				eRel.setAttribute("source", subClassId);
 				eRel.setAttribute("target", parentClassId);
 
 				Element eGeom= newGraphDoc.createElement("mxGeometry");
 				eGeom.setAttribute("as","geometry");
 				eGeom.setAttribute("relative","1");
 				eRel.appendChild(eGeom);
 
 				eRoot.appendChild(eRel);
 			}
 		}
 	}
 
 
 	private void addLineCell(Element eRoot,DomainClass clase, int heigth){
 		String sID=clase.getSID();
 
 		System.out.println("Se agrega linea separadora.");
 		
         Element eCelda = this.newGraphDoc.createElement("mxCell");
         eCelda.setAttribute("id","Linea"+sID+heigth);
         eCelda.setAttribute("parent",sID);
         eCelda.setAttribute("style","align=left;fillColor=#FFFFB6;strokeColor=#000000");
         eCelda.setAttribute("value","");
         eCelda.setAttribute("vertex","1");
 
         //System.out.println("Constante: " + com.mxgraph.util.mxConstants.STYLE_VERTICAL_ALIGN);
         Element eGeometry= this.newGraphDoc.createElement("mxGeometry");
         eGeometry.setAttribute("as","geometry");
         eGeometry.setAttribute("height","0.0");
         eGeometry.setAttribute("width","110.0");
         eGeometry.setAttribute("x","0");
         eGeometry.setAttribute("y",Integer.toString(heigth));
         
         eCelda.appendChild(eGeometry);
 		eRoot.appendChild(eCelda);
 	}
 	
 	private int addCellAttributes(Element eRoot, DomainClass clase){
 		int alto=30;
 		String sNombre=clase.getName();
 		String sID=clase.getSID();
 		
 		System.out.println("Se ejecuta addCellAttributes "+sNombre);
 		
 		Element eClass=getElementClassbyName(sNombre);
 		
 		if (eClass!=null){
 			System.out.println("Se encontro la clase: "+sNombre);
 	        Element eAttributes= (Element)eClass.getElementsByTagName("attributes").item(0);
 	        NodeList nListA = eAttributes.getElementsByTagName("attribute");
 
 	        for (int temp = 0; temp < nListA.getLength(); temp++) {
 	  		   Node nNode = nListA.item(temp);
 	  		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 					Element eAttribute= (Element) nNode;
 					String sAName=eAttribute.getAttribute("name");
 					String sAID=eAttribute.getAttribute("id");
 					String sMinCard = eAttribute.getAttribute("minCardinality");
 					String sMaxCard = eAttribute.getAttribute("maxCardinality");
 					sAName += cardinalityStringFor(sMinCard, sMaxCard);
 					
 					System.out.println("Se procesa el atributo: "+sAName);
 					
 		            Element eCelda = this.newGraphDoc.createElement("mxCell");
 		            eCelda.setAttribute("id",sAID);
 		            eCelda.setAttribute("parent",sID);
 		            eCelda.setAttribute("style","align=left;fillColor=#FFFFB6;strokeColor=#FFFFB6");
 		            eCelda.setAttribute("value","- "+sAName);
 		            eCelda.setAttribute("vertex","1");
 
 		            //System.out.println("Constante: " + com.mxgraph.util.mxConstants.STYLE_VERTICAL_ALIGN);
 		            Element eGeometry= this.newGraphDoc.createElement("mxGeometry");
 		            eGeometry.setAttribute("as","geometry");
 		            eGeometry.setAttribute("height","15.0");
 		            eGeometry.setAttribute("width","90.0");
 		            eGeometry.setAttribute("x","10");
 		            alto=temp * 15 + 25;
 		            eGeometry.setAttribute("y",Integer.toString(alto));
 		            
 		            eCelda.appendChild(eGeometry);
 					eRoot.appendChild(eCelda);
 	  		   }  		   
 	        }
 		}
 		return alto;
 	}
 	
 	private String cardinalityStringFor(String sMinCard, String sMaxCard) {
 		if (sMinCard.equals("1.0") && sMaxCard.equals("1.0")) {
 			return "";
 		}
 		Double min = Double.parseDouble(sMinCard);
 		if (sMaxCard.equals("Infinity")) {
 			return " [ " + min.intValue() + " .. * ]";
 		} else {
 			Double max = Double.parseDouble(sMaxCard);
 			return " [ " + min.intValue() + " .. " + max.intValue() + " ]";
 		}
 	}
 
 
 	private Element getElementClassbyName(String sNombre){
 		System.out.println("Se busca elemento por Nombre: " + sNombre);
 		Element e = null;
 		
         Element eClasses= (Element)dominioDoc.getElementsByTagName("classes").item(0);
         NodeList nListC = eClasses.getElementsByTagName("class");
         for (int temp = 0; temp < nListC.getLength(); temp++) {
   		   Node nNode = nListC.item(temp);
   		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
   				  Element eClass= (Element) nNode;
   			   	  String sName=eClass.getAttribute("name");
   			   	  String relId=eClass.getAttribute("id");
   				  if (sName.compareTo(sNombre)==0){
   					  e=eClass;
   					  System.out.println("Se encontro el elemento.");
   				  }
   		   }
         }
         
 		return e;
 	}
 	
 	private Element getElementGeometrybyID(String sId, String sName){
 		Element e = null;
 
 		System.out.println("Se busca celda con id: "+sId+ " Nombre: "+sName);
 		
 		NodeList celdas = this.graphDoc.getElementsByTagName("mxCell");
         
 		int temp=0;
 		boolean encontrado=false;
         while (temp < celdas.getLength() && !encontrado) {
 			Node nNode = celdas.item(temp);
 			Element eCell= (Element) nNode;
 			String eId=eCell.getAttribute("id");
 			Pattern regex = Pattern.compile(sId);			
 			Matcher matcher = regex.matcher(eId);
 			boolean matchFound = matcher.find();
 			if (matchFound) {
 				  encontrado=true;
 				  e=(Element)eCell.getFirstChild();
 			}			
 			temp++;
         }
         
         if (!encontrado){ // Si no lo encontro es un atributo compuesto
     		temp=0;
 			//System.out.println("Se busca atributo con nombre: "+sName);    		
             while (temp < celdas.getLength() && !encontrado) {
     			Node nNode = celdas.item(temp);
     			Element eCell= (Element) nNode;
     			String eId=eCell.getAttribute("id");
     			Pattern regex = Pattern.compile("Attribute(.*)"+sName);			
     			Matcher matcher = regex.matcher(eId);
     			boolean matchFound = matcher.find();
     			if (matchFound) {
     				  encontrado=true;
   					  //System.out.println("Se encontro atributo con nombre: "+sName);    				  
     				  e=(Element)eCell.getFirstChild();
     			}
     			temp++;
             }
         }
         
 		return e;
 	}
 }
