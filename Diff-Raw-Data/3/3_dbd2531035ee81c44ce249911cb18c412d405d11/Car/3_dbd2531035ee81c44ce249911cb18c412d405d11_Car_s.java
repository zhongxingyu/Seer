 import java.util.Date;
 
 public class Car {
 	String carYandexId;
 	String model;
 	int year;
 	int price;
 	Integer mileage = null;
 	Double engineCap = null;
 	String info;
 	String imgUrl;
 	String retailer;
 	String city;
 	Date date;
 	String similarCarYandexId;
 	String colour;
 	String condition;
 
 	public Car(String carYandexId, String model, int year, int price, String imgUrl, String retailer,
 			   String info, Double engineCap, Integer mileage, String city, Date date, String colour, String condition, String similarCarYandexId) {
 		this.carYandexId = carYandexId;
 		this.model = model;
 		this.year = year;
 		this.price = price;
 		this.imgUrl = imgUrl;
 		this.retailer = retailer;
 		this.info = info;
 		this.mileage = mileage;
 		this.engineCap = engineCap;
 		this.city = city;
 		this.date = date;
 		this.colour = colour;
 		this.condition = condition;
 		if (similarCarYandexId != null)
 			this.similarCarYandexId = similarCarYandexId;
 		else
 			this.similarCarYandexId = this.carYandexId;
 	}
 
 	public boolean isSimilar(Car car) {
		if (this.year != car.year || !isModelSimilar(car) || !isEngineCapSimilar(car) || !this.city.equals(car.city))
 			return false;
 
 		if (isImgSimilar(car)) {
 			return true;
 		}
 		if (diffDateDay(car) < 5 && (Math.abs(car.price - price) < 40000) && (getDiffMileage(car) < 40000))
 			return true;
 		return false;
 	}
 
 	private boolean isColourSimilar(Car car) {
 		if (car.colour == null || colour == null)
 			return true;
 		return colour.equals(car.colour);
 	}
 
 	private boolean isConditionSimilar(Car car) {
 		if (car.condition == null || condition == null)
 			return true;
 		return condition.equals(car.condition);
 	}
 
 	private boolean isImgSimilar(Car car) {
 		if (this.imgUrl == null || car.imgUrl == null)
 			return false;
 		if (this.imgUrl.equals(car.imgUrl))
 			return true;
 		//opencv will be here))
 		return false;
 	}
 
 	private long diffDateDay(Car car) {
 		if (this.date == null || car.date == null)
 			return 0;
 		long diff = this.date.getTime() - car.date.getTime();
 		final long day = 24 * 60 * 60 * 1000;
 		return diff / day;
 	}
 
 	private boolean isModelSimilar(Car car) {
 		// may be Levenshtein  distance  will be better
 
 		if (this.model.length() > car.model.length())
 			return car.isModelSimilar(this);
 		String str = car.model.substring(0, this.model.length());
 		return str.equals(model);
 	}
 
 	public int getDiffMileage(Car car) {
 		if (car.mileage == null || mileage == null)
 			return 0;
 		// error in advertisment 130000 130
 		if (car.mileage < 1000)
 			car.mileage *= 1000;
 		if (mileage < 1000)
 			mileage *= 1000;
 		return Math.abs(car.mileage - mileage);
 	}
 
 	private boolean isEngineCapSimilar(Car car) {
 		return car.engineCap == null || this.engineCap == null || car.engineCap.equals(this.engineCap);
 	}
 
 	public String toString() {
 		StringBuilder str = new StringBuilder();
 		str.append(carYandexId);
 		str.append('\n');
 		str.append(model);
 		str.append('\n');
 		str.append(imgUrl);
 		str.append('\n');
 		str.append(retailer);
 		str.append('\n');
 		str.append(year);
 		str.append('\n');
 		str.append(price);
 		str.append('\n');
 		str.append(info);
 		str.append('\n');
 		str.append(city);
 		str.append('\n');
 		str.append(date);
 		str.append('\n');
 		return str.toString();
 	}
 }
