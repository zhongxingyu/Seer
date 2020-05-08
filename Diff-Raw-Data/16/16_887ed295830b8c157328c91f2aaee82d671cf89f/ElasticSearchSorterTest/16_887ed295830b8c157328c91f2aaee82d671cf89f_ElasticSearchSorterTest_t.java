 package com.aconex.scrutineer.elasticsearch;
 
 import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import com.aconex.scrutineer.IdAndVersion;
 import com.fasterxml.sort.Sorter;
import com.google.common.io.CountingInputStream;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 public class ElasticSearchSorterTest {
 
     @Mock
     private Sorter<IdAndVersion> sorter;
     @Mock
     private InputStream inputstream;
     @Mock
     private OutputStream outputstream;
 
     @Before
     public void setup() {
         initMocks(this);
     }
 
     @Test
     public void shouldSortInputStream() throws IOException {
         ElasticSearchSorter elasticSearchSorter = new ElasticSearchSorter(sorter);
         elasticSearchSorter.sort(inputstream,outputstream);
        verify(sorter).sort(isA(CountingInputStream.class), eq(outputstream));
     }
 }
