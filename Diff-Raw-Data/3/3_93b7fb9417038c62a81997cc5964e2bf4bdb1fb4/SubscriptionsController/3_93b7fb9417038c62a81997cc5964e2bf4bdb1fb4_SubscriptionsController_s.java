 /**
  *
  * Copyright (c) 2012, PetalsLink
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package controllers;
 
 import java.util.List;
 
 import org.ow2.play.governance.api.BootSubscriptionService;
 import org.ow2.play.governance.api.GovernanceExeption;
 import org.ow2.play.governance.api.SubscriptionRegistry;
 import org.ow2.play.governance.api.SubscriptionService;
 import org.ow2.play.governance.api.bean.Subscription;
 import org.ow2.play.governance.api.bean.Topic;
 
 import utils.Locator;
 
 /**
  * @author chamerling
  * 
  */
 public class SubscriptionsController extends PlayController {
 
 	/**
 	 * Get all the subscriptions
 	 * 
 	 */
 	public static void subscriptions() {
 		SubscriptionRegistry registry = null;
 		try {
 			registry = Locator.getSubscriptionRegistry(getNode());
 		} catch (Exception e) {
 			handleException("Locator error", e);
 		}
 
 		List<Subscription> subscriptions = null;
 		try {
 			subscriptions = registry.getSubscriptions();
 		} catch (GovernanceExeption e) {
 			e.printStackTrace();
 		}
 		render(subscriptions);
 	}
 
 	public static void subscription(String id) {
 		handleException("Null id", new Exception(
 				"Can not get subscription from null ID"));
 
 		SubscriptionRegistry registry = null;
 		try {
 			registry = Locator.getSubscriptionRegistry(getNode());
 		} catch (Exception e) {
 			handleException("Locator error", e);
 		}
 
 		Subscription filter = new Subscription();
 		filter.setId(id);
 		List<Subscription> subscriptions = null;
 		try {
 			subscriptions = registry.getSubscriptions(filter);
 		} catch (GovernanceExeption e) {
 			e.printStackTrace();
 		}
 
 		Subscription subscription = null;
 		if (subscriptions != null && subscriptions.size() > 0) {
 			subscription = subscriptions.get(0);
 		}
 		render(subscription);
 	}
 	
 	/**
 	 * Create page from null args
 	 * 
 	 */
 	public static void create() {
 		createFrom("", "", "");
 	}
 
 	/**
 	 * GET
 	 * 
 	 * Create page from args
 	 * 
 	 * @param topicname
 	 * @param topicns
 	 * @param topicprefix
 	 */
 	public static void createFrom(String topicname, String topicns, String topicprefix) {
 		// flash parameters to inject them in the template (this flash stuff is also used when validating form data
 		params.flash();
 		
 		renderTemplate("SubscriptionsController/create.html");
 	}
 
 	/**
 	 * POST. Creates a subscription.
 	 * 
 	 * @param consumer
 	 * @param provider
 	 * @param topicName
 	 * @param topicNS
 	 * @param topicPrefix
 	 */
 	public static void createNew(String consumer, String provider,
 			String topicname, String topicns, String topicprefix, boolean save) {
 		
 		validation.required(consumer);
 		validation.required(provider);
 		
 		// validation url does not allow IP address...
 		validation.isTrue(consumer != null && (consumer.startsWith("http://") || consumer.startsWith("https://")));
 		validation.isTrue(provider != null && (provider.startsWith("http://") || provider.startsWith("https://")));
 		
 		validation.required(topicname);
 		validation.url(topicns);
 		validation.required(topicprefix);
 
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			createFrom(topicname, topicns, topicprefix);
 		}
 		
 		try {
 			SubscriptionService client = Locator
 					.getSubscriptionService(getNode());
 
 			Subscription subscription = new Subscription();
 			subscription.setProvider(provider);
 			subscription.setSubscriber(consumer);
 			Topic topic = new Topic();
 			topic.setName(topicname);
 			topic.setNs(topicns);
 			topic.setPrefix(topicprefix);
 			subscription.setTopic(topic);
 
 			Subscription result = client.subscribe(subscription);
 
 			if (result != null) {
 				flash.success("Subscription has been created %s", result.toString());
 			}
 			
 			if (result != null && save) {
 				// register
 				SubscriptionRegistry registry = Locator
 						.getSubscriptionRegistry(getNode());
 				registry.addSubscription(result);
 			}
 
 		} catch (GovernanceExeption ge) {
 			handleException("Can not subscribe", ge);
 		} catch (Exception e) {
 			handleException("Locator error", e);
 		}
 
 		// Forward to subscription service
 		create();
 	}
 
 	/**
 	 * GET
 	 * 
 	 * Remove all the subscriptions from the registry. This does not means that
 	 * we unregister, we just delete from storage, that's all...
 	 */
 	public static void removeAll() {
 		try {
 			SubscriptionRegistry client = Locator
 					.getSubscriptionRegistry(getNode());
 			
 			client.removeAll();
 			flash.success("Subscriptions have been removed");
 			
 		} catch (Exception e) {
 			handleException("Problem while getting client", e);
 		}
 		subscriptions();
 	}
 	
 	/**
 	 * POST
 	 * Create a new boot subscription. The subscription is just registered in
 	 * the database and is intented to be used at boot time...
 	 * 
 	 * @param consumer
 	 * @param provider
 	 * @param topicname
 	 * @param topicns
 	 * @param topicprefix
 	 * @param save
 	 */
 	public static void createNewBoot(String name, String consumer, String provider,
 			String topicname, String topicns, String topicprefix) {
 		
 		validation.required(name);
 		validation.required(consumer);
 		validation.required(provider);
 		
 		// validation url does not allow IP address...
 		validation.isTrue(consumer != null && (consumer.startsWith("http://") || consumer.startsWith("https://")));
 		validation.isTrue(provider != null && (provider.startsWith("http://") || provider.startsWith("https://")));
 		
 		validation.required(topicname);
 		validation.url(topicns);
 		validation.required(topicprefix);
 
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			bootSubscriptions(name, consumer, provider, topicname, topicns, topicprefix);
 		}
 		
 		try {
 			BootSubscriptionService client = Locator
 					.getBootSubscriptionService(getNode());
 
 			Subscription subscription = new Subscription();
 			subscription.setId(name);
 			subscription.setProvider(provider);
 			subscription.setSubscriber(consumer);
 			
 			Topic topic = new Topic();
 			topic.setName(topicname);
 			topic.setNs(topicns);
 			topic.setPrefix(topicprefix);
 			
 			subscription.setTopic(topic);
 			subscription.setDate(System.currentTimeMillis());
 
 			client.add(subscription);
 
 			flash.success("Subscription has been created");
 			
 		} catch (GovernanceExeption ge) {
 			handleException("Can not create boot subscription", ge);
 		} catch (Exception e) {
 			handleException("Locator error", e);
 		}
 
 		bootSubscriptions(null, null, null, null, null, null);
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 * @param consumer
 	 * @param provider
 	 * @param topicname
 	 * @param topicns
 	 * @param topicprefix
 	 */
 	public static void deleteBoot(String name, String consumer, String provider,
 			String topicname, String topicns, String topicprefix) {
 		
 		validation.required(name);
 		validation.required(consumer);
 		validation.required(provider);
 		
 		// validation url does not allow IP address...
 		validation.isTrue(consumer != null && (consumer.startsWith("http://") || consumer.startsWith("https://")));
 		validation.isTrue(provider != null && (provider.startsWith("http://") || provider.startsWith("https://")));
 		
 		validation.required(topicname);
 		validation.url(topicns);
 		validation.required(topicprefix);
 
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			bootSubscriptions(name, consumer, provider, topicname, topicns, topicprefix);
 		}
 		
 		try {
 			BootSubscriptionService client = Locator
 					.getBootSubscriptionService(getNode());
 
 			Subscription subscription = new Subscription();
 			subscription.setId(name);
 			subscription.setProvider(provider);
 			subscription.setSubscriber(consumer);
 			
 			Topic topic = new Topic();
 			topic.setName(topicname);
 			topic.setNs(topicns);
 			topic.setPrefix(topicprefix);
 			
 			subscription.setTopic(topic);
 			subscription.setDate(System.currentTimeMillis());
 
 			client.remove(subscription);
 
 			flash.success("Subscription has been removed");
 			
 		} catch (GovernanceExeption ge) {
 			handleException("Can not delete boot subscription", ge);
 		} catch (Exception e) {
 			handleException("Locator error", e);
 		}
 
 		bootSubscriptions(null, null, null, null, null, null);
 	}
 	
 	/**
 	 * List the boot subscriptions
 	 */
 	public static void bootSubscriptions(String name, String consumer, String provider,
 			String topicname, String topicns, String topicprefix) {
 		
 		params.flash();
 
 		BootSubscriptionService client = null;
 		try {
 			client = Locator.getBootSubscriptionService(getNode());
 		} catch (Exception e) {
 			handleException("Locator error", e);
 		}
 
 		List<Subscription> subscriptions = null;
 		try {
 			subscriptions = client.all();
 		} catch (GovernanceExeption e) {
 			e.printStackTrace();
 		}
		render(subscriptions);
 	}
 }
