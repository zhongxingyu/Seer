 package org.netvogue.server.webmvc.converters;
 
 import org.netvogue.server.aws.core.ImageType;
 import org.netvogue.server.aws.core.Size;
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.webmvc.domain.Collection;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.converter.Converter;
 
 public class CollectionConverter implements Converter<org.netvogue.server.neo4japi.domain.Collection, Collection> {
 
 	@Autowired
 	private UploadManager uploadManager;
 	
 	public Collection convert(org.netvogue.server.neo4japi.domain.Collection source) {
 		Collection newCollection = new Collection();
 		newCollection.setGalleryid(source.getCollectionid());
 		newCollection.setGalleryname(source.getCollectionname());
 		String thumblink = uploadManager.getQueryString(source.getProfilePicLink(), ImageType.COLLECTION, Size.CThumb);
 		newCollection.setGallerypic(thumblink);
 		newCollection.setGallerydesc(source.getDescription());
 		newCollection.setBrandname(source.getCreatedBy().getName());
 		newCollection.setCategory(source.getProductcategory().getProductLine().getDesc());
 		return newCollection;
 	}
 
 }
