 package com.mmounirou.spoty4j.xml;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.List;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.ext.MessageBodyReader;
 
 import org.apache.commons.digester3.Digester;
 import org.xml.sax.SAXException;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.mmounirou.spoty4j.core.Album;
 import com.mmounirou.spoty4j.core.Artist;
 import com.mmounirou.spoty4j.xml.AlbumSearchResultProvider.AlbumSearchResult;
 
 public class AlbumSearchResultProvider implements MessageBodyReader<AlbumSearchResult>
 {
 	public static final class AlbumSearchResult
 	{
 
 		private List<Album> m_albums = Lists.newArrayList();
 
 		public ImmutableList<Album> getAlbums()
 		{
 			return ImmutableList.copyOf(m_albums);
 		}
 
 		public void addAlbum(Album album)
 		{
 			m_albums.add(album);
 		}
 
 	}
 
 	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
 	{
		return type.isAssignableFrom(AlbumSearchResult.class);
 	}
 
 	public AlbumSearchResult readFrom(Class<AlbumSearchResult> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
 			InputStream entityStream) throws IOException, WebApplicationException
 	{
 		Digester digester = new Digester();
 		addRules(digester);
 
 		try
 		{
 			return digester.parse(entityStream);
 		}
 		catch ( SAXException e )
 		{
 			throw new IOException(e);
 		}
 	}
 
 	private void addRules(Digester digester)
 	{
 		digester.addObjectCreate("albums", AlbumSearchResult.class);
 
 		digester.addObjectCreate("albums/album", Album.class);
 		digester.addSetNext("albums/album", "addAlbum");
 
 		digester.addBeanPropertySetter("albums/album/name");
 		digester.addBeanPropertySetter("albums/album/id");
 		digester.addBeanPropertySetter("albums/album/popularity");
 		digester.addBeanPropertySetter("albums/album/name");
 		digester.addBeanPropertySetter("albums/album/availability/territories");
 		digester.addSetProperties("albums/album", "href", "href");
  
 		digester.addObjectCreate("albums/album/artist", Artist.class);
 		digester.addSetNext("albums/album/artist", "setArtist");
 
 		digester.addSetProperties("albums/album/artist", "href", "href");
 		digester.addBeanPropertySetter("albums/album/artist/name");
 
 	}
 
 }
