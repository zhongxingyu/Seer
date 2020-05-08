 package com.madalla.service.cms;
 
import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 class ImageDataHelper extends AbstractContentHelper {
 	
 	// Repository Values
     static final String EC_NODE_IMAGES = NS + "images";
     static final String EC_PROP_TITLE = NS + "title";
     private static final String EC_IMAGE_FULL = "imageFull";
     private static final String EC_IMAGE_THUMB = "imageThumb";
     static final String EC_PROP_DESCRIPTION = NS + "description";
     
     private static final Log log = LogFactory.getLog(ImageDataHelper.class);
 	private static ImageDataHelper instance;
 	public static ImageDataHelper getInstance(){
 		return instance;
 	}
 
     private static ImageData create(Node node) throws RepositoryException{
     	String parent = node.getParent().getName().replaceFirst(NS,"");
     	String name = node.getName().replaceFirst(NS, "");
     	InputStream image = node.getProperty(EC_IMAGE_FULL).getStream();
     	ImageData data = new ImageData(node.getPath(), parent, name, image);
     	data.setDescription(node.getProperty(EC_PROP_DESCRIPTION).getString());
     	data.setTitle(node.getProperty(EC_PROP_TITLE).getString());
     	return data;
     }
     
 	static DynamicImageResource createImageResource(InputStream inputStream){
		DynamicImageResource webResource = null;
 		try {
			final BufferedImage bufferedImage = ImageIO.read(inputStream);
			webResource = new DynamicImageResource(){

				private static final long serialVersionUID = 1L;

				@Override
				protected byte[] getImageData() {
					return toImageData(bufferedImage);
				}
				
			};
 		} catch (IOException e) {
 			log.error("Exception while reading image from Content.",e);
 			throw new WicketRuntimeException("Error while reading image from Content Management System.");
 		}
 		return webResource;
 		
 	}
 
 	public ImageDataHelper(String site, JcrTemplate template ){
 		this.site = site;
 		this.template = template;
 		instance = this;
 	}
 	
 	String save(final ImageData imageData) {
 		return genericSave(imageData);
     }
 	
 	ImageData get(final String path){
 		return (ImageData) template.execute(new JcrCallback(){
 			public Object doInJcr(Session session) throws IOException,	RepositoryException {
 				Node node = (Node) session.getItem(path);
 				return create(node);
 			}
 			
 		});
 	}
 	
 	
 	
 	@SuppressWarnings("unchecked")
 	List<ImageData> getEntries(final String group){
 		return (List<ImageData>) template.execute(new JcrCallback(){
             List<ImageData> list = new ArrayList<ImageData>();
             public List<ImageData> doInJcr(Session session) throws IOException, RepositoryException {
             	Node siteNode = getSiteNode(session);
             	Node parentNode = getParentNode(siteNode);
             	Node groupNode  = getCreateNode(NS+group, parentNode);
                 
                 for (NodeIterator iterator = groupNode.getNodes(); iterator.hasNext();){
                     Node nextNode = iterator.nextNode();
                     list.add(create(nextNode));
                 }
                 return list;
             }
         });
 	}
     
 	@Override
     Node getParentNode(Node node) throws RepositoryException{
     	return getCreateNode(EC_NODE_IMAGES, node);
     }
 
 	@Override
 	void setPropertyValues(Node node, IRepositoryData data)
 			throws RepositoryException {
 		ImageData imageData = (ImageData) data;
 		node.setProperty(EC_PROP_TITLE, imageData.getTitle());
 		node.setProperty(EC_PROP_DESCRIPTION, imageData.getDescription());
 		node.setProperty(EC_IMAGE_FULL, imageData.getFullImageAsInputStream());
 		
 	}
 	
 
 }
