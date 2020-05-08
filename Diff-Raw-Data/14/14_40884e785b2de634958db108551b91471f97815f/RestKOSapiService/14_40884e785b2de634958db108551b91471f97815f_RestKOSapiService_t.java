 /*
  * The MIT License
  *
  * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package cz.cvut.portal.kos.services;
 
 import cz.cvut.portal.kos.model.Course;
 import cz.cvut.portal.kos.services.support.ListPaginator;
 import cz.cvut.portal.kos.services.support.Paginator;
 import cz.cvut.portal.kos.services.support.ListPaginator.DataFetcher;
 import cz.cvut.portal.kos.utils.UriBuilder;
 import cz.jirutka.atom.jaxb.Entry;
 import cz.jirutka.atom.jaxb.Feed;
 import java.net.URI;
 import java.util.List;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestOperations;
 import org.springframework.web.client.RestTemplate;
 
 /**
  *
  * @author Jakub Jirutka <jakub@jirutka.cz>
  */
 public class RestKOSapiService implements KOSapiService {
 
     private static final String PARAM_LIMIT = "limit";
     private static final String PARAM_OFFSET = "offset";
     private static final char[] ILLEGAL_CHARS = {'\'', '"', '&', '?'};
 
    private RestOperations rest;
 
     // will be set from properties file
     private @Value("${kosapi.uri.base}") String baseUri;
     private @Value("${kosapi.uri.courses.query-simple}") String coursesQuerySimpleUri;
     private @Value("${kosapi.uri.courses.query-rsql}") String coursesQueryRsqlUri;
     private @Value("${kosapi.uri.courses.get}") String coursesGetUri;
 
 
     
     public Paginator<Course> findCoursesByCodeOrName(final String codeOrName) {
         validateArgument(codeOrName);
 
         return new ListPaginator<>(new DataFetcher<Course>() {
             public List<Course> fetchPage(int itemsPerPage, int startIndex) {
                 URI url = path(coursesQuerySimpleUri)
                             .expand("query", codeOrName)
                             .queryParam(PARAM_LIMIT, itemsPerPage)
                             .queryParam(PARAM_OFFSET, startIndex)
                             .build();

                // pass as String to perform encoding
                return rest.getForObject(url.toString(), Feed.class).getContents();
             }
         });
     }
 
     public Course getCourse(String code, int detailLevel) {
         validateArgument(code);
 
         URI url = path(coursesGetUri)
                     .expand("code", code)
                     .queryParam("detail", detailLevel)
                     .build();
 
        // pass as String to perform encoding
        Entry<Course> entry = rest.getForObject(url.toString(), Entry.class);
         return entry.getContent();
     }
 
 
     private UriBuilder path(String path) {
         return UriBuilder.fromBase(baseUri).path(path);
     }
 
     private void validateArgument(String str) {
         if (StringUtils.isBlank(str)) {
             throw new IllegalArgumentException("Argument is blank");
         }
         if (StringUtils.containsAny(str, ILLEGAL_CHARS)) {
             throw new IllegalArgumentException("Argument contains illegal characters");
         }
     }
 
     ////////  Accessors  ////////
 
     public void setRestTemplate(RestTemplate restTemplate) {
         this.rest = restTemplate;
     }
 
 }
