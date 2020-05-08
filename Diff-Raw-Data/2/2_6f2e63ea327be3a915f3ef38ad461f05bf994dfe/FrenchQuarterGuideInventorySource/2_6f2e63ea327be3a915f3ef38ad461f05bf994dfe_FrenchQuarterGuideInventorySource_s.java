 package com.nodc.scraper.inventory;
 
 import java.math.BigDecimal;
 import java.net.URI;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import com.nodc.scraper.dao.HotelDetailCacheKey;
 import com.nodc.scraper.dao.HotelDetailDAO;
 import com.nodc.scraper.dao.MasterHotelDAO;
 import com.nodc.scraper.dao.SourceHotelDAO;
 import com.nodc.scraper.model.DailyRate;
 import com.nodc.scraper.model.Hotel;
 import com.nodc.scraper.model.RoomType;
 import com.nodc.scraper.model.SearchParams;
 import com.nodc.scraper.model.SearchResult;
 import com.nodc.scraper.model.persisted.InventorySource;
 import com.nodc.scraper.model.persisted.SourceHotel;
 import com.google.code.ssm.api.format.SerializationType;
 import com.google.common.collect.Lists;
 
 public class FrenchQuarterGuideInventorySource implements Warehouse
 {
 	private static final Logger logger = LoggerFactory.getLogger(FrenchQuarterGuideInventorySource.class);		
 	private static final DateTimeFormatter STAY_DATE_FORMAT = DateTimeFormat.forPattern("MM/dd/yyyy");
 	
 	private static final ExecutorService asyncResultsThreadPool = Executors.newCachedThreadPool();
 	
 	private HotelDetailDAO hotelDetailDAO;
 	private SourceHotelDAO sourceHotelDAO;
 	private MasterHotelDAO masterHotelDAO;
 	
  // @Autowired
  // @Qualifier("defaultMemcachedClient") 
   private com.google.code.ssm.Cache cache;
 	
 	@Override
 	public Collection<Hotel> 
 		getInitialResultsAndAsyncContinue(HttpServletRequest request, final SearchParams params) throws Exception
 	{
 		Collection<Hotel> hotels = Lists.newArrayList();
 		List<Callable<Collection<Hotel>>> workers = Lists.newArrayList();
 		for (int i=1; i < 7; i++)
 		{
 			final int idx = i;
 			workers.add(new Callable<Collection<Hotel>>()
 			{
 				@Override
 				public Collection<Hotel> call() throws Exception
 				{
 					HttpResponse resp = queryHotelsViaHttpClient(params, idx);
 					Collection<Hotel> indResult = Lists.newArrayList();
 					if (resp != null)
 					{
 						byte[] html = EntityUtils.toByteArray(resp.getEntity());
 						indResult = createHotels(params, html);
 					}
 					return indResult;
 				}
 			});
 		}
 		
 		for (Future<Collection<Hotel>> result: asyncResultsThreadPool.invokeAll(workers, 35, TimeUnit.SECONDS))
 		{
 			try
 			{
 				Collection<Hotel> indResultHotels = result.get();
 				if (indResultHotels != null)
 					hotels.addAll(indResultHotels);
 			}
 			catch (Exception e)
 			{
 				logger.error("Unable to add in results", e);
 			}
 		}
 		
 		String cacheKey = params.getSessionInfo().getSessionId()+InventorySource.FQG.name();
 		logger.debug(String.format("CACHE: fqg cache key (%1$s); num hotels stored: " + hotels.size(), cacheKey));
 		request.getSession().setAttribute(InventorySource.FQG.name(), hotels);
 		
 		logger.debug("fqg complete");
 		return hotels;
 	}
 	
 	private Collection<Hotel> createHotels(SearchParams params, byte[] html) throws Exception
 	{
 		Document document = Jsoup.parse(
 				new String(html), "http://hotelsearch.neworleans.com/js/ajax/city_page_redesign/getResults.php");
 		
 		List<Hotel> hotels = Lists.newArrayList();
 		for (Element hotelElement: document.select(".hotelbox"))
 		{
 			if (Thread.interrupted())
 			{
 				logger.warn("Stopping result gathering; thread has been cancelled");
 				break;				
 			}
 
 			String extHotelId = hotelElement.id();
 			logger.error("parsing hotel id: " + extHotelId);
 			com.nodc.scraper.model.persisted.InventorySource invSource = 
 					com.nodc.scraper.model.persisted.InventorySource.FQG;
 					
 			SourceHotel sourceHotel = sourceHotelDAO.getByHotelId(extHotelId, invSource);
 			if (sourceHotel == null)
 			{
 				logger.warn("unable to find mapping for: " + extHotelId);
 				continue;
 			}
 			Hotel hotel = new Hotel();
 			hotel.setName(sourceHotel.getHotelName());
 			hotel.setSource(sourceHotel);
 			hotel.setHotelDetails( hotelDetailDAO.getHotelDetail(new HotelDetailCacheKey(sourceHotel.getHotelName(), InventorySource.FQG)));
 			if (hotel.getHotelDetails() == null)
 			{
 				logger.warn("unable to find hotel details for: " + extHotelId + ", " + sourceHotel.getHotelName());
 				continue;
 			}
 			
 			if (hotel.getHotelDetails() != null)
 				hotel.getHotelDetails().setWeight(masterHotelDAO.getByHotelName(sourceHotel.getHotelName()).getWeight());
 			Elements els = hotelElement.select(".room");
 			els.addAll(hotelElement.select(".more_rooms1 .room"));
 			//for (Element rtElement: hotelElement.select(".room"))
 			for (Element rtElement: els)
 			{
 				RoomType rt = new RoomType();
 				String onclick = rtElement.select(".room_price").first().attr("onclick");
 				int begIdx = onclick.indexOf("'");
 				int endIdx = onclick.indexOf("');");
 				String[] idParts = onclick.substring(begIdx, endIdx).split(",");
 				StringBuffer roomTypeId = new StringBuffer();
 				for (int i=0; i < idParts.length; i++)
 				{
 					String idPart = StringUtils.strip(idParts[i], "'");
 					idPart = StringUtils.strip(idPart, "'");
 					idPart = StringUtils.strip(idPart, ")");
 					idParts[i] = idPart;
 					roomTypeId.append(idPart);
 					if (i+1 != idParts.length)
 						roomTypeId.append("_");
 				}
 				rt.name = rtElement.select(".roomlink .room_type a").first().ownText();
 				rt.avgNightlyRate = InventoryUtils.createMoney(
 						hotelElement.select("#rateDetails_"+roomTypeId + " .price").first().ownText());
 				
 				LocalDate currentDate = params.getCheckInDate();
 				BigDecimal totalPrice = BigDecimal.ZERO;
 				for (Element dailyRateEl: hotelElement.select("#rateDetails_"+roomTypeId + " .rateDetails tbody td"))
 				{
 					if (dailyRateEl.ownText().startsWith("$"))
 					{
 						DailyRate dRate = new DailyRate();
 						dRate.date = currentDate.toDate();
 						dRate.originalPrice = InventoryUtils.createMoney(dailyRateEl.ownText());
 						dRate.price = InventoryUtils.createMoney(dailyRateEl.ownText());
 						currentDate = currentDate.plusDays(1);
 						totalPrice = totalPrice.add(dRate.price);
 						rt.getDailyRates().add(dRate);
 					}
 				}
 				rt.totalPrice = totalPrice; //InventoryUtils.createMoney(rtElement.select(".room_price .price").first().ownText());
 				
 				rt.bookItUrl=createBookUrl(params, idParts);
 				hotel.addRoomType(rt);
 			}
 			hotels.add(hotel);
 		}
 		return hotels;
 	}
 	
 	private String createBookUrl(SearchParams sp, String[] idParts)
 	{
 		StringBuffer url = new StringBuffer();
		url.append("https://hotelsearch.neworleans.com/book/index.php?refid=5057&seshid=ac5711115d836fd9a54f2c0757438cfc");
 		url.append("refid=5057");
 		url.append("&rs_hid="+idParts[0]);
 		url.append("&rs_rate_cat="+idParts[1]);
 		url.append("&rs_rate_code="+idParts[2]);
 		url.append("&rs_room_code="+idParts[3]);
 		url.append("&rs_city=New Orleans, LA");
 		url.append("&rs_chk_in="+STAY_DATE_FORMAT.print(sp.getCheckInDate()));
 		url.append("&rs_chk_out="+STAY_DATE_FORMAT.print(sp.getCheckOutDate()));
 		url.append("&rs_rooms="+sp.getNumRooms());
 		url.append("&ts_testing=");
 		url.append("&_booknow=1");
 		return url.toString();
 	}
 	
 
 	private HttpResponse queryHotelsViaHttpClient(SearchParams sp, int page)
 	{
 		URIBuilder builder = new URIBuilder();
 		builder
 				.setScheme("http")
 				.setHost("hotelsearch.neworleans.com")
 				.setPath("/js/ajax/city_page_redesign/getResults.php")
 				.addParameter("rs_city", "New Orleans, Lousiana")
 				.addParameter("rs_cid", "3000008434")
 				.addParameter("rs_chk_in", STAY_DATE_FORMAT.print(sp.getCheckInDate()))
 				.addParameter("rs_chk_out", STAY_DATE_FORMAT.print(sp.getCheckOutDate()))
 				.addParameter("rs_rooms", String.valueOf(sp.getNumRooms()))
 				.addParameter("rs_curr_code", "")
 				.addParameter("rs_m_km", "")
 				.addParameter("needLiveRates", "true")
 				.addParameter("rs_page", String.valueOf(page))
 				.addParameter("rs_sort", "mp")
 				.addParameter("refid", "5057")
 				.addParameter("disableCache","");
 		HttpResponse response = null;
 		try
 		{
 			URI uri = builder.build();
 			HttpGet httpget = new HttpGet(uri);
 			logger.error("uri == " + httpget.getURI());
 
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			HttpParams httpParams = httpClient.getParams();
 			HttpConnectionParams.setConnectionTimeout(httpParams, 45000);
 	    HttpConnectionParams.setSoTimeout(httpParams, 45000);
 			response = httpClient.execute(httpget);
 		} 
 		catch (Exception e)
 		{
 			logger.error("unable to retrieve query response", e);
 		}
 		return response;
 	}	
 	
 	public void setHotelDetailDAO(HotelDetailDAO hDAO)
 	{
 		this.hotelDetailDAO = hDAO;
 	}
 	
 	public void setSourceHotelDAO(SourceHotelDAO dao)
 	{
 		this.sourceHotelDAO = dao;
 	}
 	
 	public void setMasterHotelDAO(MasterHotelDAO dao)
 	{
 		this.masterHotelDAO = dao;
 	}
 	
 }
