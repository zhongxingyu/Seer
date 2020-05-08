 /**
  *
  * Copyright (c) 2013, Linagora
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
 import org.ow2.play.governance.api.SubscriptionManagement;
 import org.ow2.play.governance.api.bean.Subscription;
 import org.ow2.play.governance.api.bean.Topic;
 
 import play.mvc.With;
 import utils.Locator;
 
 /**
  * @author chamerling
  * 
  */
 @With(Secure.class)
 public class BootstrapController extends PlayController {
 	
 	/**
 	 * GET
 	 * 
	 * Form display
 	 * 
 	 * @param name
 	 * @param consumer
 	 * @param provider
 	 * @param topicname
 	 * @param topicns
 	 * @param topicprefix
 	 */
 	public static void create(String name, String consumer, String provider,
 			String topicname, String topicns, String topicprefix) {
 		params.flash();
 		render();
 	}
 
 	/**
 	 * List subscriptions which will be replayed...
 	 */
 	public static void subscriptions() {
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
 
 	/**
 	 * Launch all the boot subscriptions. All subscriptions will be launched
 	 * here!
 	 */
 	public static void launchBootSubscriptions() {
 		
 		BootSubscriptionService client = null;
 		try {
 			client = Locator.getBootSubscriptionService(getNode());
 		} catch (Exception e) {
 			handleException("Locator error", e);
 		}
 
 		SubscriptionManagement subscriptionService = null;
 		try {
 			subscriptionService = Locator.getSubscriptionManagement(getNode());
 		} catch (Exception e) {
 			handleException("Locator error", e);
 		}
 
		List<Subscription> bootSubscriptions = null;
 		try {
			bootSubscriptions = client.all();
 		} catch (GovernanceExeption e) {
 			e.printStackTrace();
 		}
 
		List<Subscription> subscriptions = null;
		if (bootSubscriptions != null) {
 			try {
				subscriptions = subscriptionService.subscribe(bootSubscriptions);
 			} catch (GovernanceExeption e) {
 				flash.error("Error while creating subscriptions '%s'",
 						e.getMessage());
 			}
 		}
 		renderTemplate("BootstrapController/subscriptionsresult.html",
 				subscriptions);
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
 			create(name, consumer, provider, topicname, topicns, topicprefix);
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
 
 		subscriptions();
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
 		
 		flash.error("Delete is not implemented");
 		subscriptions();
 		
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
 			create(name, consumer, provider, topicname, topicns, topicprefix);
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
 
 		subscriptions();
 	}
 
 }
