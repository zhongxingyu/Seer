 package com.lunch4you.web.controller;
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dozer.Mapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.lunch4you.domain.Article;
 import com.lunch4you.domain.ArticleWithOrders;
 import com.lunch4you.domain.DeliveryLocation;
 import com.lunch4you.domain.DeliveryLocationWithArticles;
 import com.lunch4you.domain.Order;
 import com.lunch4you.service.MenuService;
 import com.lunch4you.web.dto.ArticleDto;
 import com.lunch4you.web.dto.ArticleWithOrdersDto;
 import com.lunch4you.web.dto.DeliveryLocationDto;
 import com.lunch4you.web.dto.DeliveryLocationWithArticlesDto;
 import com.lunch4you.web.dto.OrderDto;
 import com.lunch4you.web.dto.OrderItemDto;
 
 @Controller
 public class OrderController {
 
 	private static final Logger logger = LoggerFactory.getLogger( OrderController.class );
 
 	@Autowired
 	private MenuService menuService;
 
 	@Autowired
 	private Mapper beanMapper;
 
 	@RequestMapping( value = "/orders", method = RequestMethod.POST )
 	public @ResponseBody
 	OrderDto createNew( @RequestBody Map<String, Object> data ) {
 		logger.trace( "OrderController.createNew called with data: " + data );
 
 		long articleId = Long.parseLong( data.get( "articleId" ).toString() );
 		long customerId = Long.parseLong( data.get( "customerId" ).toString() );
 		long deliveryLocationId = Long.parseLong( data.get( "deliveryLocationId" ).toString() );
 
 		Order order = menuService.createOrder( articleId, customerId, deliveryLocationId );
 		OrderDto orderDto = mapOrder( order );
 
 		return orderDto;
 	}
 
 	@RequestMapping( value = "/backoffice/orders/close.json", method = RequestMethod.PUT )
 	public @ResponseBody
 	List<Long> close( @RequestBody List<String> intIds ) {
 		// IDs mapped as List<Integer> - bug?
 		List<Long> ids = new ArrayList<Long>( intIds.size() );
 		for ( String id : intIds )
 			ids.add( Long.parseLong( id ) );
 		logger.trace( "OrderController.close called with ID-s: " + ids );
 
 		List<Long> missingOrders = menuService.closeOrders( ids );
 
 		return missingOrders;
 	}
 
 	@RequestMapping( value = "/backoffice/orders/delete.json", method = RequestMethod.PUT )
 	public @ResponseBody
	List<Long> delete( @RequestBody List<String> intIds ) {
 		// IDs mapped as List<Integer> - bug?
 		List<Long> ids = new ArrayList<Long>( intIds.size() );
 		for ( String id : intIds )
 			ids.add( Long.parseLong( id ) );
 		logger.trace( "OrderController.delete called with ID-s: " + ids );
 
 		menuService.deleteOrders(ids);
 		logger.trace( "OrderController.delete called with ID-s: " + ids );
		return Collections.emptyList();
 	}
 
 	@RequestMapping( value = "/backoffice/orders/activeByDate.json", method = RequestMethod.GET )
 	public @ResponseBody
 	List<OrderDto> findActive() {
 		logger.trace( "OrderController.findActive called" );
 
 		List<Order> orders = menuService.getActiveOrders();
 		List<OrderDto> orderDtos = mapOrderList( orders );
 
 		return orderDtos;
 	}
 
 	private OrderDto mapOrder( Order o ) {
 		OrderDto orderDto = beanMapper.map( o, OrderDto.class );
 		orderDto.onlyItem = beanMapper.map( o.getItems().get( 0 ), OrderItemDto.class );  // TODO: temporary solution only - remove later
 		return orderDto;
 	}
 
 	private List<OrderDto> mapOrderList( List<Order> orders ) {
 		List<OrderDto> orderDtos = new ArrayList<OrderDto>( orders.size() );
 		
 		for ( Order o : orders ) {
 			OrderDto orderDto = mapOrder( o );
 			orderDtos.add( orderDto );
 		}
 		return orderDtos;
 	}
 
 	@RequestMapping( value = "/backoffice/orders/activeGroupedByArticle.json", method = RequestMethod.GET )
 	public @ResponseBody
 	List<ArticleWithOrdersDto> getActiveOrdersGroupedByArticle() {
 		
 		LinkedHashMap<Long,ArticleWithOrders> articlesWithOrders = menuService.getActiveOrdersByArticle();
 		List<ArticleWithOrdersDto> articlesWithOrdersDtos = new ArrayList<ArticleWithOrdersDto>( );
 
 		for ( ArticleWithOrders articleWithOrders : articlesWithOrders.values() ) {
 			ArticleWithOrdersDto articleWithOrdersDto = new ArticleWithOrdersDto();
 
 			articleWithOrdersDto.entity = beanMapper.map( articleWithOrders.entity, ArticleDto.class );
 
 			for ( Order order : articleWithOrders.items.values() )
 				articleWithOrdersDto.items.add( beanMapper.map( order, OrderDto.class ) );
 
 			articlesWithOrdersDtos.add( articleWithOrdersDto );
 		}
 		return articlesWithOrdersDtos;
 	}
 
 	@RequestMapping( value = "/backoffice/orders/activeGroupedByDeliveryLocation.json", method = RequestMethod.GET )
 	public @ResponseBody
 	List<DeliveryLocationWithArticlesDto> getActiveOrdersGroupedByDeliveryLocation() {
 		
 		LinkedHashMap<Long,DeliveryLocationWithArticles> locationsWithArticles = menuService.getActiveOrdersByDeliveryLocation();
 		
 		List<DeliveryLocationWithArticlesDto> locationsWithArticlesDtos = new ArrayList<DeliveryLocationWithArticlesDto>();
 
 		for ( DeliveryLocationWithArticles deliveryLocationWithArticles : locationsWithArticles.values() ) {
 			DeliveryLocation deliveryLocation = deliveryLocationWithArticles.entity;
 			DeliveryLocationWithArticlesDto deliveryLocationWithArticlesDto = new DeliveryLocationWithArticlesDto();
 			deliveryLocationWithArticlesDto.entity = beanMapper.map( deliveryLocation, DeliveryLocationDto.class );
 			deliveryLocationWithArticlesDto.countOrderItems = deliveryLocationWithArticles.countOrderItems;
 			
 			for ( ArticleWithOrders articleWithOrders : deliveryLocationWithArticles.items.values() ){				
 				Article article = articleWithOrders.entity;
 
 				// Construct complete DTO object before it is added to parent group DTO
 				ArticleWithOrdersDto articleWithOrdersDto = new ArticleWithOrdersDto();												
 				articleWithOrdersDto.entity = beanMapper.map( article, ArticleDto.class );
 
 				for(Order order : articleWithOrders.items.values()){
 					OrderDto orderDto = beanMapper.map( order, OrderDto.class );
 					articleWithOrdersDto.items.add(orderDto);										
 				}
 				deliveryLocationWithArticlesDto.items.add(articleWithOrdersDto);
 			}
 			locationsWithArticlesDtos.add( deliveryLocationWithArticlesDto );
 		}
 		return locationsWithArticlesDtos;
 	}
 
 }
