 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.webapp.api.resources;
 
 import com.google.common.collect.Maps;
 import com.maxmind.geoip.Location;
 import com.maxmind.geoip.LookupService;
 import freemarker.template.TemplateException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Map;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 import org.lafayette.server.core.log.Log;
 import org.lafayette.server.core.log.Logger;
 import org.lafayette.server.web.http.Constants;
 import org.lafayette.server.web.http.MediaType;
 import org.lafayette.server.web.http.UriList;
 import org.lafayette.server.webapp.api.template.Layout;
 import org.lafayette.server.webapp.api.template.Template;
 
 /**
  * Serves the REST API for the geo IP service.
  *
  * See https://github.com/snambi/GeoIP/blob/master/src/test/java/com/maxmind/geoip/GeoLiteCityTest.java
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 @Path("/service/geoip")
 public class GeoIpServiceResource extends BaseResource {
 
     /**
      * Index Markdown template file name.
      */
     private static final String INDEX_MARKDOWN = "api.service.geoip.index.md";
     /**
      * Index title string.
      */
     private static final String INDEX_TITLE = "GeoIP Services";
     /**
      * Prefix for service resource files.
      */
     private static final String RESOURCE_PREFIX = "/org/lafayette/server/webapp/api/geoip";
     /**
      * IPv4 database resource.
      */
     private static final String GEO_IP_DATABASE_V4 = RESOURCE_PREFIX + "/GeoLiteCity.dat";
     /**
      * IPv6 database resource.
      */
     private static final String GEO_IP_DATABASE_V6 = RESOURCE_PREFIX + "/GeoLiteCityv6.dat";
     /**
      * IPv4 database URL.
      */
     private static final URL LOCATION_V4 = GeoIpServiceResource.class.getClassLoader().getResource(GEO_IP_DATABASE_V4);
     /**
      * IPv6 database URL.
      */
     private static final URL LOCATION_V6 = GeoIpServiceResource.class.getClassLoader().getResource(GEO_IP_DATABASE_V6);
     /**
      * USed for not available information.
      */
     private static final String NOT_AVAILABLE = "n/a";
     /**
      * Logger facility.
      */
     private final Logger log = Log.getLogger(DataServiceResource.class);
     /**
      * IPv4 lookup.
      */
     private final LookupService lookupV4;
     /**
      * IPv6 lookup.
      */
    private final LookupService lookupV6; // NOPMD
 
     /**
      * Dedicated constructor.
      *
      * @throws IOException if lookup databases can't be read
      */
     public GeoIpServiceResource() throws IOException {
         super();
         lookupV4 = new LookupService(LOCATION_V4.getFile(), LookupService.GEOIP_MEMORY_CACHE);
         lookupV6 = new LookupService(LOCATION_V6.getFile(), LookupService.GEOIP_MEMORY_CACHE);
     }
 
     @Override
     protected void addUrisToIndexList(final UriList indexUriList) throws URISyntaxException {
         log.debug("Add URIs to index list.");
         indexUriList.add(createRelativeUri("/{ip}"));
     }
 
     @Override
     protected String indexAsMarkdown() throws TemplateException, IOException {
         final Template tpl = createTemplate(INDEX_MARKDOWN);
         assignBaseVariables(tpl);
         return tpl.render();
     }
 
     @GET
     @Path("/{ip}")
     @Produces(MediaType.TEXT_HTML)
     public Response ipAsHtml(@PathParam("ip") final String ip) throws IOException, TemplateException {
         log.debug(String.format("Lookup ip %s (HTML).", ip));
         final Location location = lookupV4.getLocation(ip);
 
         if (null == location) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
 
         final Layout layout = createLayout();
         final Template tpl = createTemplate("api.service.geoip.ip.md");
         assignBaseVariables(tpl);
         assignVariables(tpl, ip, location);
         layout.setContent(processMarkdown(tpl.render()));
         return Response.ok(layout.render(), MediaType.TEXT_HTML).build();
     }
 
     private String checkForNull(final float in) {
         return checkForNull(String.valueOf(in));
     }
 
     private String checkForNull(final String in) {
         if (null == in) {
             return NOT_AVAILABLE;
         }
 
         if (in.isEmpty()) {
             return NOT_AVAILABLE;
         }
 
         return in;
     }
 
     @GET
     @Path("/{ip}")
     @Produces(MediaType.TEXT_PLAIN)
     public Response ipAsPlainText(@PathParam("ip") final String ip) throws IOException {
         log.debug(String.format("Lookup ip %s (plain).", ip));
         final Location location = lookupV4.getLocation(ip);
 
         if (null == location) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
 
         return Response.ok(location.toString() + Constants.NL, MediaType.TEXT_PLAIN).build();
     }
 
     @GET
     @Path("/{ip}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response ipAsJson(@PathParam("ip") final String ip) {
         log.debug(String.format("Lookup ip %s (JSON).", ip));
         final Location location = lookupV4.getLocation(ip);
 
         if (null == location) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
 
         final Map<String, Object> tpl = Maps.newHashMap();
         tpl.put("ip", ip);
         tpl.put("latitude", location.latitude);
         tpl.put("longitude", location.longitude);
         tpl.put("city", checkForNull(location.city));
         tpl.put("region", checkForNull(location.region));
         tpl.put("countryName", checkForNull(location.countryName));
         tpl.put("countryCode", checkForNull(location.countryCode));
         tpl.put("postalCode", checkForNull(location.postalCode));
 
         return Response.ok(tpl, MediaType.APPLICATION_JSON).build();
     }
 
     @GET
     @Path("/{ip}")
     @Produces(MediaType.APPLICATION_XML)
     public Response ipAsXml(@PathParam("ip") final String ip) throws IOException, TemplateException {
         log.debug(String.format("Lookup ip %s (XML).", ip));
         final Location location = lookupV4.getLocation(ip);
 
         if (null == location) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
 
         final Template tpl = createTemplate("api.service.geoip.ip.xml");
         assignVariables(tpl, ip, location);
 
         return Response.ok(tpl.render(), MediaType.APPLICATION_XML).build();
     }
 
     @Override
     protected String getIndexTitle() {
         return INDEX_TITLE;
     }
 
     private void assignVariables(final Template tpl, final String ip, final Location location) {
         tpl.assignVariable("ip", ip);
         tpl.assignVariable("latitude", checkForNull(location.latitude));
         tpl.assignVariable("longitude", checkForNull(location.longitude));
         tpl.assignVariable("city", checkForNull(location.city));
         tpl.assignVariable("region", checkForNull(location.region));
         tpl.assignVariable("countryName", checkForNull(location.countryName));
         tpl.assignVariable("countryCode", checkForNull(location.countryCode));
         tpl.assignVariable("postalCode", checkForNull(location.postalCode));
     }
 }
