 package eu.deustotech.internet.linkedtagworld.lib;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.text.SpannableString;
 import android.text.style.UnderlineSpan;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 import eu.deustotech.internet.linkedtagworld.layout.Layout;
 import eu.deustotech.internet.linkedtagworld.model.Widget;
 import eu.deustotech.internet.linkedtagworld.vocabulary.LTW;
 import eu.deustotech.internet.linkedtagworld.model.ClassItem;
 import eu.deustotech.internet.linkedtagworld.model.PropertyItem;
 
 import org.xml.sax.SAXException;
 
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.concurrent.ExecutionException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 /**
  * Created by mikel on 21/05/13.
  */
 /**
  * Modified by jon on 04/06/13.
  */
 /**
  * Main class of library. It loads configuration file, gets RDF data and renders Android application.
  * @author mikel, jon 
  */
 public class LinkedTagWorld {
 
     private Context context;
     private Activity activity;
     private InputStream inputStream;
 
     private static final Map<String, String> uri2prefixMap = new HashMap<String, String>();
     private static final Map<String, String> prefix2uriMap = new HashMap<String, String>();
 
     private String className;
 
     private List<String> prefixedTypeList;
     
     private Model model;
     
 	private static final List<ClassItem> configuration = new ArrayList<ClassItem>();
 	
 	private String lang;
 
 	/**
 	 * Default constructor. Loads configuration file.
 	 */
     public LinkedTagWorld() {
     	loadConfiguration();
     }
 
     /**
      * Constructor. Load configuration file and assigns context, activity and inputStream properties.
      * 
      * @param context the context of the Android Activity where the constructor is called.
      * @param activity the Android Activity itself.
      * @param inputStream the InputStream from configuration file.
      */
     public LinkedTagWorld(Context context, Activity activity, InputStream inputStream) {
         this.context = context;
         this.activity = activity;
         this.inputStream = inputStream;
         loadConfiguration();
     }
 
     /**
      * Renders RDF data from URI in corresponding layout.
      * 
      * @param URI the URI wich contains the RDF resource. It supports 303 redirect, slash URIs, hash URIs and so on.
      * @param lang the lang to be retrieved from the RDF resource, in <a target="_blank" href="http://www.loc.gov/standards/iso639-2/php/code_list.php">ISO 639-1</a> code format.
      * @throws ParserConfigurationException
      * @throws IOException
      * @throws SAXException
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws ClassNotFoundException
      * @throws InterruptedException
      * @throws ExecutionException
      */
     public void renderData(String URI, String lang) throws ParserConfigurationException, IOException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, InterruptedException, ExecutionException {
 
         this.model = setModel(URI);
         this.lang = lang;
         
         if (this.model.size() > 0) {
         	this.prefixedTypeList = getPrefixedTypeList(this.model);
         	ClassItem classItem = getClassItem(this.prefixedTypeList);
         	this.activity.setTitle(getMain(URI));
 	        Map<String, Widget> propertyMap = getPropertyMap(classItem);
 	        setLayout(propertyMap);
         }
 
     }
 
 	private void loadConfiguration() {
 		if (LinkedTagWorld.configuration.isEmpty()) {			
 			Model model = ModelFactory.createDefaultModel();
 				
 			model.read(this.inputStream, null, "TURTLE");
 			
 			fillPrefixesMaps(model.getNsPrefixMap());
 	
 			ResIterator iter =  model.listResourcesWithProperty(RDF.type, LTW.ClassItem);
 			
 			while (iter.hasNext()) {
 			    Resource res = iter.nextResource();
 			    
 			    ClassItem classItem = new ClassItem();
 			    
 			    StmtIterator stmts = res.listProperties(LTW.javaClass);
 			    while (stmts.hasNext()) {
 					classItem.setJavaClass(stmts.nextStatement().getObject().toString());
 				}
 			    stmts = res.listProperties(LTW.ontologyClass);
 			    while (stmts.hasNext()) {
 					classItem.setOntologyClass(stmts.nextStatement().getObject().toString());
 				}
 			    stmts = res.listProperties(LTW.identifier);
 			    while (stmts.hasNext()) {
 					classItem.setIdentifier(stmts.nextStatement().getObject().toString());
 				}
 			    
 			    List<PropertyItem> propertyItems = new ArrayList<PropertyItem>(); 
 		    
 			    StmtIterator propItems = res.listProperties(LTW.hasPropertyItem);
 			    
 			    while (propItems.hasNext()) {
 			    	Statement stmt = propItems.nextStatement();
 			    	Resource blankNodeUri = (Resource) stmt.getObject();
 			    	
 			    	PropertyItem propertyItem = new PropertyItem();
 			    	
 				    stmts = blankNodeUri.listProperties(LTW.identifier);
 				    while (stmts.hasNext()) {
 				    	propertyItem.setIdentifier(stmts.nextStatement().getObject().toString());
 					}
 				    stmts = blankNodeUri.listProperties(LTW.ontologyProperty);
 				    while (stmts.hasNext()) {
 				    	propertyItem.setOntologyProperty(stmts.nextStatement().getObject().toString());
 					}
 				    stmts = blankNodeUri.listProperties(LTW.isLinkable);
 				    while (stmts.hasNext()) {
 				    	propertyItem.setIsLinkable(Boolean.valueOf(stmts.nextStatement().getObject().toString()));
 					}
 				    stmts = blankNodeUri.listProperties(LTW.isClickable);
 				    while (stmts.hasNext()) {
 				    	propertyItem.setIsClickable(Boolean.valueOf(stmts.nextStatement().getObject().toString()));
 					}
 				    stmts = blankNodeUri.listProperties(LTW.isMain);
 				    while (stmts.hasNext()) {
 				    	propertyItem.setIsMain(Boolean.valueOf(stmts.nextStatement().getObject().toString()));
 					}
 				    stmts = blankNodeUri.listProperties(LTW.datatype);
 				    while (stmts.hasNext()) {
 				    	propertyItem.setDatatype(stmts.nextStatement().getObject().toString());
 				    }
 				    stmts = blankNodeUri.listProperties(LTW.language);
 				    while (stmts.hasNext()) {
 				    	propertyItem.setLanguage(stmts.nextStatement().getObject().toString());
 				    }
 				    
 				    if (propertyItem.validate())
 				    	propertyItems.add(propertyItem);
 			    }
 			    
 			    classItem.setPropertyItems(propertyItems);		    
 			    if (classItem.validate())
 			    	LinkedTagWorld.configuration.add(classItem);
 			}
 		}
 	}    
     
     private void fillPrefixesMaps(Map<String, String> configPrefixes) {
     	LinkedTagWorld.prefix2uriMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
     	LinkedTagWorld.uri2prefixMap.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
 
         for(Entry<String, String> entry : configPrefixes.entrySet()) {
         	LinkedTagWorld.prefix2uriMap.put(entry.getKey(), entry.getValue());
         	LinkedTagWorld.uri2prefixMap.put(entry.getValue(), entry.getKey());
         }
     }
 
     private Model setModel(String URI) throws IOException, InterruptedException, ExecutionException {
  	
     	return new RetrieveHTTPData().execute(URI).get();
     	
     	/*
         HttpClient client = new DefaultHttpClient();
         HttpGet request = new HttpGet(URI);
         request.addHeader("Accept", "application/rdf+xml");
 
         HttpResponse response = client.execute(request);
         InputStream inputStream = response.getEntity().getContent();
 
         Model model = ModelFactory.createDefaultModel();
         
         model.read(inputStream, null);
         
         return model;
         */
         
     }
     
     private List<String> getPrefixedTypeList(Model model) {
     	List<String> typeList = new ArrayList<String>();
     	NodeIterator typeNodes = model.listObjectsOfProperty(RDF.type);
         
         while (typeNodes.hasNext()) {
         	RDFNode node = typeNodes.nextNode();
         	if(node.isResource()) {
         		Resource res = (Resource) node;
         		String type = res.getURI();
         		typeList.add(type);
         	}
         }
         
         List<String> prefixedTypeList = new ArrayList<String>();
 
         for (String type : typeList) {
 
             prefixedTypeList.add(String.format("%s:%s", LinkedTagWorld.uri2prefixMap.get(getPrefix(type)), type.split(getPrefix(type))[1]));
         }
         
         return prefixedTypeList;
     }
     
     private String getPrefix(String URI) {
         if (URI.contains("#")) {
             return URI.split("#")[0] + "#";
         } else {
             String [] splitURI = URI.split("/");
             String prefixURI = "";
             for (int i = 0; i < splitURI.length - 1; i++) {
                 prefixURI += splitURI[i] + "/";
             }
             return prefixURI;
         }
     }
 
     private ClassItem getClassItem(List<String> prefixedTypeList) {
     	for (String type : prefixedTypeList) {
     		String extendedClass = LinkedTagWorld.prefix2uriMap.get(type.split(":")[0]) + type.split(":")[1];
     		for (ClassItem item : LinkedTagWorld.configuration) {
     			if (extendedClass.equals(item.getOntologyClass())) {
     				this.className = item.getJavaClass();
     				return item;
     			}
     		}
     	}
     	return null;
     }
 
     private Map<String, Widget> getPropertyMap(ClassItem classItem) {
         Map<String, Widget> propertyMap = new HashMap<String, Widget>();
         
         for (PropertyItem item : classItem.getPropertyItems()) {
         	propertyMap.put(item.getOntologyProperty(), new Widget(item.getIdentifier(), item.getIsClickable(), item.getIsMain()));
         }
         
         return propertyMap;
     }
 
     private void setLayout(Map<String, Widget> propertyMap) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, InterruptedException, ExecutionException {	
     	Class<Layout> layoutClass = (Class<Layout>) Class.forName(this.className);
         Layout layout = layoutClass.newInstance();
 
         this.activity.setContentView(layout.getLayout());
 
         Map<String, Integer> widgetMap = layout.getWidgets();
 
         for (String property : propertyMap.keySet()) {
         	Property prop = this.model.getProperty(property);
         	NodeIterator nodeIterator = this.model.listObjectsOfProperty(prop);
         	
         	while(nodeIterator.hasNext()) {
         		Widget widget = propertyMap.get(property);
         		String id = widget.getId();
         		RDFNode node = nodeIterator.next();
         		String object = "";
         		if (node.isLiteral()) {
         			Literal literalNode = (Literal) node;
         			if (!literalNode.getLanguage().equals("")) {
         				if (!literalNode.getLanguage().equals(this.lang)) {
         					continue;
         				}
         			}
         			object = literalNode.getString();
         		} else if (node.isResource()) {
         			Resource resNode = (Resource) node;
         			object = resNode.getURI();
         		}
         		int viewID = widgetMap.get(id);
         		Object view = this.activity.findViewById(viewID);
         		setView(object, view, null, widget);
         	}
         }
 
         clearLayouts(widgetMap);
 
     }
 
     private void clearLayouts(Map<String, Integer> widgetMap) {
         for (String key : widgetMap.keySet()) {
             Integer widgetID = widgetMap.get(key);
             View view = this.activity.findViewById(widgetID);
             if (ViewGroup.class.isAssignableFrom(view.getClass())) {
                 ViewGroup viewGroup = (ViewGroup) view;
                 viewGroup.removeView(viewGroup.getChildAt(0));
             }
         }
     }
 
     private void setView(final String object, Object view, Object originalView, Widget widget) throws IOException, InterruptedException, ExecutionException {
         if (view instanceof TextView) {
             TextView textView = (TextView) view;
             String template = "";
             if (originalView == null) {
                 template = textView.getText().toString();
             } else {
                 TextView originalTextView = (TextView) originalView;
                 template = originalTextView.getText().toString();
             }
             String text = "";
             if (widget.isClickable()) {
             	
                 String strObject = getMain(object);
                 if (strObject != null) {
 	                try {
 	                    //Statement statement = main.get(0);
 	                    text = String.format(template, strObject);
 	
 	                    textView.setOnClickListener( new View.OnClickListener() {
 	                        @Override
 	                        public void onClick(View view) {
 	                            Intent intent = new Intent(context.getApplicationContext(), activity.getClass());
 	                            intent.putExtra("getBoolean", false);
 	                            intent.putExtra("URI", object);
 	
 	                            activity.startActivity(intent);
 	                        }
 	                    });
 	                } catch (NullPointerException e) {
 	                    text = String.format(template, object);
 	                }
                 
                 }
                 
                 SpannableString content = new SpannableString(text);
                 content.setSpan(new UnderlineSpan(), 0, text.length(), 0);
                 textView.setText(content);
 
             } else {
                 text = String.format(template, object);
                 textView.setText(text);
             }
             
 
 
 
         } else if (view instanceof ViewGroup) {
             ViewGroup layoutView = (ViewGroup) view;
             View child = layoutView.getChildAt(0);
             View childCopy = null;
             try {
                 String childClassStr = child.getClass().getCanonicalName();
                 System.out.println(childClassStr);
                 Class<View> childClass = (Class<View>) Class.forName(child != null ? child.getClass().getCanonicalName() : null);
 
                 Class[] constructorArgs = new Class[1];
                 constructorArgs[0] = Context.class;
                 Constructor<View> constructor = childClass.getDeclaredConstructor(constructorArgs);
                 childCopy = constructor.newInstance(this.context);
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             } catch (NoSuchMethodException e) {
                 e.printStackTrace();
             } catch (InvocationTargetException e) {
                 e.printStackTrace();
             } catch (InstantiationException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
             setView(object, childCopy, child, widget);
             childCopy.setVisibility(View.VISIBLE);
             childCopy.setLayoutParams(new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)));
             layoutView.addView(childCopy);
 
 
         } else if (view instanceof ImageView) {
             ImageView imageView = (ImageView) view;
             Bitmap bm = new RetrieveHTTPBitmap().execute(object).get();
 			imageView.setImageBitmap(bm);
 			//imageView.setImageBitmap(BitmapFactory.decodeStream(new URL(object).openConnection().getInputStream()));
 
         }
     }
 
     private String getMain(String URI) throws IOException, InterruptedException, ExecutionException {
         //Collection<Statement> statementCollection = setModel(URI);
     	Model uriModel = setModel(URI);
         if (uriModel.size() > 0) {
 	        //Map<String, List<Statement>> predicateMap = getPredicateMap(statementCollection);
         	List<String> prefixedTypeList = getPrefixedTypeList(uriModel);
 	        
         	ClassItem classItem = getClassItem(prefixedTypeList);
 	        //Map<String, Widget> propertyMap = getPropertyMap(pageNode);
 	        //Map<String, Widget> propertyMap = new HashMap<String, Widget>();
        	PropertyItem propItem = classItem.getMainPropertyItem();
         	
            if (propItem != null) {
            	Property prop = uriModel.getProperty(propItem.getOntologyProperty());
                NodeIterator nodeIterator = uriModel.listObjectsOfProperty(prop);
                
                while(nodeIterator.hasNext()) {
                	RDFNode rdfNode = nodeIterator.next();
                	if (rdfNode.isLiteral()) {
                		Literal literalNode = (Literal) rdfNode;
                		return literalNode.getString();
                	}
                }
            }
         }
         return null;
     }
 }
