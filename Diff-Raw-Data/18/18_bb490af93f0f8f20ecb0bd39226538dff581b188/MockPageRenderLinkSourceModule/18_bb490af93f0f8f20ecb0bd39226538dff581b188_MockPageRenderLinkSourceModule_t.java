 
 package axirassa.ioc;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import org.apache.tapestry5.Link;
 import org.apache.tapestry5.ioc.MappedConfiguration;
 import org.apache.tapestry5.services.PageRenderLinkSource;
 
 public class MockPageRenderLinkSourceModule {
	public static void contributeServiceOverride(MappedConfiguration<Class<?>, Object> configuration) {
 		Link mockLink = mock(Link.class);
 		when(mockLink.toAbsoluteURI()).thenReturn("http://axirassa/");
 		when(mockLink.toAbsoluteURI(true)).thenReturn("https://axirassa/");
 		when(mockLink.toAbsoluteURI(false)).thenReturn("http://axirassa/");
 
 		PageRenderLinkSource mockPrls = mock(PageRenderLinkSource.class);
 		when(mockPrls.createPageRenderLinkWithContext(any(Class.class), anyObject())).thenReturn(mockLink);
 		configuration.add(PageRenderLinkSource.class, mockPrls);
 	}
 }
