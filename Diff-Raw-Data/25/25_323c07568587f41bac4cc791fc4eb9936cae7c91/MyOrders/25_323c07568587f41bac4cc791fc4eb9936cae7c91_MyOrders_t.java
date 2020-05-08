 package controllers;
 
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.Map;
 
 import models.LineItem;
 import models.Meal;
 import models.MealType;
 import models.Order;
 import models.Restaurant;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.Logger;
 import play.mvc.With;
 import play.utils.Utils;
 import utils.Result;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 @With(Secure.class)
 public class MyOrders extends BaseController {
 
 	public static void booking(MealType mealType) {
 		Logger.debug("[booking] meal type: %s", mealType);
 		if (mealType == null) {
 			notFound();
 		}
 
 		Restaurant restaurant = Restaurant.getTodayRestaurant(mealType);
		Order order = null;
 		if (restaurant == null || !restaurant.allowBooking(mealType)) {
			render(restaurant, order);
 		}
		order = mealType.isLunch() ? Order.getLunchOrderToday(Security.connectedUser()) : Order
 				.getSupperOrderToday(Security.connectedUser());
 		if (order == null) {
 			order = new Order(Security.connectedUser(), mealType);
 		}
 		List<Meal> meals = restaurant.getAvailableMeals(mealType);
 		Logger.debug("[booking] Available meals for %s from %s:\n- %s", mealType, restaurant.name,
 				Utils.join(meals, "\n- "));
		render(order, restaurant, meals);
 	}
 
 	public static void edit(Long orderId) {
 		Order order = Order.findById(orderId);
 		if (order == null) {
 			notFound();
 		}
 		Restaurant restaurant = order.getTodayRestaurant();
 		List<Meal> meals = restaurant.getAvailableMeals(order.type);
 		render(order, restaurant, meals);
 	}
 
 	public static void save(MealType type, String lineItems) {
 		Logger.debug("[MyOrders#save] type: %s, lineItems: %s", type, lineItems);
 		if (type == null || StringUtils.isBlank(lineItems)) {
 			notFound();
 		}
 
 		Type jsonType = new TypeToken<List<Map<String, String>>>() {
 		}.getType();
 		List<Map<String, String>> lineItemsGot = new Gson().fromJson(lineItems, jsonType);
 		if (lineItemsGot.isEmpty()) {
 			notFound();
 		}
 
 		Restaurant todayRestaurant = Restaurant.getTodayRestaurant(type);
 		if (!todayRestaurant.allowBooking(type)) {
 			renderJSON(Result.error("您已错过了订餐，或者当前没有餐饭可订"));
 		}
 
 		Order order = type.isLunch() ? Order.getLunchOrderToday(Security.connectedUser()) : Order
 				.getSupperOrderToday(Security.connectedUser());
 		if (order.isPersistent()) {
 			LineItem.delete("order = ?", order);
 			order.refresh();
 		} else {
 			order = new Order(Security.connectedUser(), type);
 		}
 
 		for (Map<String, String> item : lineItemsGot) {
 			if (item.get("meal") == null || item.get("quantity") == null) {
 				notFound();
 			}
 			Long mealId = null;
 			Integer quantity = null;
 			try {
 				mealId = new Long(item.get("meal"));
 				quantity = new Integer(item.get("quantity"));
 			} catch (NumberFormatException e) {
 				notFound();
 			}
 			Meal meal = Meal.findById(mealId);
 			if (meal == null) {
 				renderJSON(Result.error("餐饭已不存在，请刷新页面后重试"));
 			}
 			if (quantity < 0 || quantity > 3) {
 				renderJSON(Result.error("每份数量限制为 1 到 3"));
 			}
 			if (!todayRestaurant.id.equals(meal.provider.id)) {
 				renderJSON(Result.error(String.format("今天只提供 %s 的餐饭，请刷新页面后重试", todayRestaurant.name)));
 			}
 			order.addMeal(meal, quantity);
 		}
 		Logger.debug("Saving order... %s", order.type);
 		order.save();
 		renderJSON(Result.ok);
 	}
 
 	public static void cancel(Long orderId) {
 		Order order = Order.findById(orderId);
 		if (order == null) {
 			notFound();
 		}
 		Logger.info("%s tries to cancel order: %s", Security.connected(), order);
 		if (!order.canBeModified()) {
 			renderJSON(Result.error("订餐截至时间已过，不能取消订餐。请与负责订餐的人员联系：xuhuihui, 分机号: 8530"));
 		} else {
 			order.delete();
 			renderJSON(Result.ok);
 		}
 	}
 }
