 package rest.resources;
 
 import hibernate.db.DB_Process;
 import jabx.model.ChartModel;
 import jabx.model.SerieModel;
 import static utils.ServerUtils.NULL_VAL;
 import java.lang.reflect.InvocationTargetException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.common.primitives.Doubles;
 
 public class ChartRes {
 	@Context UriInfo uriInfo;
 	@Context Request request;
 	private static int TYPE_AVERAGE=1;
 	private static int TYPE_WIDTH=2;
 	private static int TYPE_ORIGINAL=3;
 	private static int TYPE_WIDTH_2=4;
 
 
 	private int id;
 	private String user_email;
 	public ChartRes(UriInfo uriInfo, Request request, int id, String user_email) {
 		this.uriInfo = uriInfo;
 		this.request = request;
 		this.id = id;
 		this.user_email=user_email;
 	}
 
 	@GET
 	@Produces({MediaType.APPLICATION_JSON})
 	public ChartModel getChart(@DefaultValue("0") @QueryParam("x") int x, @DefaultValue("0") @QueryParam("y") int y, @DefaultValue("0") @QueryParam("month") int month,@DefaultValue("0") @QueryParam("week") int week , @QueryParam("day") int day, @QueryParam("year") int year, @DefaultValue("0") @QueryParam("type") int type) throws NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException, ParseException {
 		try{
 			ChartModel chart=DB_Process.i.getChart(id);
 			//Return the month requested by parameter month
 			if (month != 0 || day != 0 || year!=0 || week !=0){
 				List<Integer> matches=getMatches(year, month, week, day, chart.getxValues());
 				if (matches.size()==0)
 					return null;
 				modifYval(matches,chart.getyValues());
 				chart.setxValues(modifXval(matches, chart.getxValues()));
 			}
 
 			if ((x == 0 && y == 0) || type == TYPE_ORIGINAL)
 				return chart;
 
 			int numXgroups=(chart.getxValues().length + x -1) / x;
 			if (type == TYPE_WIDTH) numXgroups*=2;
 			//			int numYgroups=(series.get(0).getYvalues().length + y -1) / y;
 
 			if (numXgroups < 2)
 				return chart;
 			//Impossible to apply the reduction algorithm if there's more then one Y serie
 			if (type == TYPE_WIDTH && chart.getyValues().size() > 1)
 				type=TYPE_WIDTH_2;
 
 			System.out.println("TYPE=" + type);
 
 			//			int[] repetitions=new int[((chart.getxValues().length + numXgroups -1) / numXgroups)];
 			List<List<Integer>> matchesMatrix= new ArrayList<>();
 			//Choosing values from the Y axis (average)
 			for (SerieModel serie : chart.getyValues()) {
 				//Splitting the Y values in groups of Yval/android_screen_height
 
 				List<List<Double>> groupYval=Lists.partition(Doubles.asList(serie.getYvalues()), numXgroups);
 				System.out.println("number of sub-lists=" + groupYval.size());
 				System.out.println("Sublist size=" + groupYval.get(0).size());	
 				//Choosing the average value of each group
 				if (type == TYPE_WIDTH){
 					double result[]=new double[0];
 					for (List<Double> list : groupYval) {
 						Set<Double> uniqueDoubles= Sets.newLinkedHashSet(list);
 						System.out.println("Duplicates in the array=" + (list.size() - uniqueDoubles.size()));
 						matchesMatrix.add(getSelectedPositions(uniqueDoubles, list));
 						result=Doubles.concat(result,Doubles.toArray(uniqueDoubles));
 					}
 					serie.setYvalues(result);
 				}else if (type == 100){
 					ArrayList<Double> result=new ArrayList<>();
 					for (List<Double> list : groupYval) {
 						List<Double> uniqueDoubles= deleteDuplicates(list);
 						result.addAll(uniqueDoubles);
 					}
 					serie.setYvalues(Doubles.toArray(result));
 				}else if (type == TYPE_WIDTH_2){
 					serie.setYvalues(setMaxMinList(groupYval));
 				}
 				else
 					serie.setYvalues(meanList(groupYval));
 
 			}
 
 			//Choosing values from the X axis (average)
 			List<List<Double>> groupXval=Lists.partition(Doubles.asList(chart.getxValues()), numXgroups);
 			System.out.println("number of sub-dates=" + groupXval.size());
 			System.out.println("SubDates size=" + groupXval.get(0).size());
 			//				for (Date d : groupXval.get(0))
 			//					System.out.println(d);
 			if (type==TYPE_WIDTH){
 				double result[]=new double[0];
 				int pos=0;
 				for (List<Double> list: groupXval) {
 					List<Integer> listMatches=matchesMatrix.get(pos);
 					double[] concat=new double[listMatches.size()];
 					for (int i = 0; i < concat.length; i++) 
 						concat[i]=list.get(listMatches.get(i));
 					result=Doubles.concat(result,concat);
 					pos++;
 				}
 				chart.setxValues(result);
 			}else if (type==TYPE_WIDTH_2){
 				chart.setxValues(meanListForTwo(groupXval));
 			}
 			else if (type ==TYPE_AVERAGE)
 				chart.setxValues(meanList(groupXval));
 			//				System.out.println("get average of the first subgroup=" + chart.getxValues()[0]);
 
 
 			return chart;
 
 		}catch(NullPointerException e){
 			throw new WebApplicationException(Response.Status.NOT_FOUND);
 		}
 	}
 
 	@GET
 	@Path("series")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Set<SerieModel> getValues() throws NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException{
 		try{
 			return DB_Process.i.getChart(id).getyValues();
 		}catch(NullPointerException e){
 			throw new WebApplicationException(Response.Status.NOT_FOUND);
 		}
 	}
 
 	@Path("comments")
 	public ChartCommentRes getComments(){
 		return new ChartCommentRes(uriInfo, request, id, user_email);
 	}
 
 	private <T> T mean(List<T> array, Class<T> clazz){
 		double sum=0;
 
 		for (T val : array) {
 			if (clazz == Date.class)
 				sum+=((Date)val).getTime();
 			else
 				sum += ((Double)val);
 		}
 		try {
 			if (clazz == Date.class)
 				return clazz.getConstructor(long.class).newInstance((long)sum/array.size());
 			else
 				return clazz.getConstructor(double.class).newInstance((double)sum/array.size());
 
 		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException |InvocationTargetException | NoSuchMethodException | SecurityException e) 
 		{e.printStackTrace(); return null;} 
 	}
 
 	private double[] meanList(List<List<Double>> list){
 		double result[] = new double[list.size()];
 		int pos=0;
 		for (List<Double> values: list) {
 			result[pos] = mean(values,Double.class);
 			pos++;
 		}
 		return result;
 	}
 	private double[] meanListForTwo(List<List<Double>> list){
 		//		double result[] = new double[list.size()*2];
 		//		ArrayList<Double> result=new ArrayList<>();
 		int pos=0;
 		double result[];
 		final int pixelElems=list.get(0).size();
 		if (pixelElems > 3)
 			result = new double[list.size()*4];
 		else
 			result = new double[list.size()*2];
 
 		for (List<Double> values: list) {
 			//			result[pos]=values.get(0);
 			//			pos++;
 			//			result[pos]= values.get(values.size()-1);
 			//			pos++;
 			result[pos] = values.get(0);
 			pos++;
 			if (pixelElems > 3){
 				double mean=mean(values, Double.class);
 				result[pos]=mean-1;
 				pos++;
 				result[pos]=mean+1;
 				pos++;
 			}
 			result[pos] = values.get(values.size()-1);
 			pos++;
 		}
 		return result;
 	}
 
 	private List<Integer> getMatches(int y, int m,int w, int d, double[] xValues) throws ParseException{
 		List<Integer> result=new ArrayList<Integer>();
 		Calendar calendar= Calendar.getInstance();
 		calendar.clear();
 		calendar.set(Calendar.YEAR, y);
 
 		if (d != 0){
 			calendar.set(Calendar.MONTH, m-1);
 			calendar.set(Calendar.DAY_OF_MONTH, d);
 			long firstVal=calendar.getTimeInMillis();
 
			calendar.set(Calendar.DAY_OF_MONTH, d+1);
 			long lastVal=calendar.getTimeInMillis()-1;
 
 			for (int i = 0; i < xValues.length; i++)
 				if (xValues[i] >= firstVal && xValues[i] <= lastVal)
 					result.add(i);	
 
 			return result;
 
 		}else if (w != 0){
 			calendar.set(Calendar.MONTH, m-1);
 			if (w != 1)
 				calendar.set(Calendar.WEEK_OF_MONTH, w);
 			long firstVal=calendar.getTimeInMillis();
 
 			calendar.clear();
 			calendar.set(Calendar.YEAR, y);
 			if (w== 5)
 				calendar.set(Calendar.MONTH, m);
 			else{
 				calendar.set(Calendar.MONTH, m-1);
 				calendar.set(Calendar.WEEK_OF_MONTH, w+1);
 			}
 			long lastVal=calendar.getTimeInMillis()-1;
 			for (int i = 0; i < xValues.length; i++)
 				if (xValues[i] >= firstVal && xValues[i] <= lastVal)
 					result.add(i);
 
 			return result;
 			
 		}else if (m != 0){
 			calendar.set(Calendar.MONTH, m-1);
 			long firstVal=calendar.getTimeInMillis();
 
 			calendar.set(Calendar.MONTH, m);
 			long lastVal=calendar.getTimeInMillis()-1;
 
 			for (int i = 0; i < xValues.length; i++)
 				if (xValues[i] >= firstVal && xValues[i] <= lastVal)
 					result.add(i);
 
 			return result;
 
 		}else{
 			long firstVal=calendar.getTimeInMillis();
 			calendar.set(Calendar.YEAR, y+1);
 			long lastVal=calendar.getTimeInMillis()-1;
 			for (int i = 0; i < xValues.length; i++)
 				if (xValues[i] >= firstVal && xValues[i] <= lastVal)
 					result.add(i);
 			return result;
 		}
 	}
 
 	private int find(List<Double> array, double value) {
 		for(int i=0; i<array.size(); i++) 
 			if(array.get(i) == value)
 				return i;
 		return -1;
 	}
 	private ArrayList<Integer> getSelectedPositions(Set<Double> uniqueDoubles, List<Double> list){
 		ArrayList<Integer> result= new ArrayList<>();
 		for (Double value : uniqueDoubles) {
 			result.add(find(list, value));
 		}
 		return result;
 	}
 	private void modifYval(List<Integer> xValues, Set<SerieModel> y_results_tmp){
 
 		//Modif Y Values
 		for (SerieModel serieModel : y_results_tmp) {
 			double[] data=new double[xValues.size()];
 			for (int i=0; i<xValues.size();i++)
 				data[i]=serieModel.getYvalues()[xValues.get(i)];
 			serieModel.setYvalues(data);
 		}
 	}
 	private double[] modifXval(List<Integer> xValues, double [] x_results_tmp){
 		double[] x_results=new double[xValues.size()];
 
 		//Modif X Values
 		for (int i=0; i< xValues.size(); i++)
 			x_results[i]=x_results_tmp[xValues.get(i)];
 
 		return x_results;
 	}
 	public static List<Double> deleteDuplicates(List<Double> list){
 		List<Double> result = new ArrayList<>();
 		int rep=0;
 		for (Double value : list) {
 			if (!result.contains(value)) result.add(value);
 			else{
 				result.add(NULL_VAL);
 				rep++;
 			}
 		}
 		System.out.println("Num of replications=" + rep);
 		return result;
 	}
 	public double[] setMaxMinList(List<List<Double>> groupYval){
 		//		int pos=0;
 		final int pixelElems=groupYval.get(0).size();
 
 		//		double[] result= new double[groupYval.size()*2];
 		ArrayList<Double> result = new ArrayList<Double>();
 		for (List<Double> list : groupYval) {
 			double[] array_list=Doubles.toArray(list);
 			ArrayList<Double> newElems= new ArrayList<>();
 			newElems.add(array_list[0]);
 			newElems.add(array_list[array_list.length-1]);			
 			Double max = Doubles.min(array_list);
 			Double min = Doubles.max(array_list);
 			if (pixelElems > 3){
 				if (newElems.contains(max))
 					newElems.add(1,NULL_VAL);
 				else
 					newElems.add(1,max);
 				if (newElems.contains(min))
 					newElems.add(1,NULL_VAL);
 				else
 					newElems.add(1,min);
 			}
 			result.addAll(newElems);
 		}
 		return Doubles.toArray(result);
 	}
 
 }
