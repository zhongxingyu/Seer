 package org.gwtapp.ccalc.rpc.proc.calculator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.gwtapp.ccalc.rpc.data.book.Calculation;
 import org.gwtapp.ccalc.rpc.data.book.CalculationImpl;
 import org.gwtapp.ccalc.rpc.data.book.Currency;
 import org.gwtapp.ccalc.rpc.data.book.Operation;
 import org.gwtapp.ccalc.rpc.data.book.OperationImpl;
 
 public class Calculator {
 
 	private final static List<String> FIELDS = new ArrayList<String>();
 	static {
 		FIELDS.addAll(new CalculationImpl().getPropertyNames());
 		FIELDS.removeAll(new OperationImpl().getPropertyNames());
 	}
 
 	private final List<Calculation> calculations = new ArrayList<Calculation>();
 	private final List<Calculation> summaries = new ArrayList<Calculation>();
 	private final Map<Currency, List<Edge>> edges = new HashMap<Currency, List<Edge>>();
 	{
 		for (Currency currency : Currency.values()) {
 			edges.put(currency, new ArrayList<Edge>());
 		}
 	}
 
 	private final Currency baseCurrency;
 	private final List<Integer> summariesPoints;
 
 	public Calculator(Currency baseCurrency, List<Operation> operations) {
 		this(baseCurrency, Arrays.asList(new Integer[] { operations.size() }),
 				operations);
 	}
 
 	public Calculator(Currency baseCurrency, List<Integer> summariesPoints,
 			List<Operation> operations) {
 		this.baseCurrency = baseCurrency;
 		this.summariesPoints = summariesPoints;
 		for (Operation operation : operations) {
 			Calculation calculation = new CalculationImpl(operation);
 			for (String property : FIELDS) {
 				calculation.set(property, null);
 			}
 			calculations.add(calculation);
 		}
 		calculate();
 	}
 
 	public List<Calculation> getCalculations() {
 		return calculations;
 	}
 
 	public List<Calculation> getSummaries() {
 		return summaries;
 	}
 
 	private class Point {
 		public int i;
 		public double v;
 
 		public Point(int i, double v) {
 			this.i = i;
 			this.v = v;
 		}
 	}
 
 	public static class Edge {
 		public int x, y;
 		public double v, r;
 		public boolean d;
 
 		public Edge(int x, int y, double v, double r, boolean d) {
 			this.x = x;
 			this.y = y;
 			this.v = v;
 			this.r = r;
 			this.d = d;
 		}
 	}
 
 	public void calculate() {
 		for (Currency currency : Currency.values()) {
 			calculate(currency);
 		}
 		calculateSummary();
 	}
 
 	private void calculateSummary() {
 		Iterator<Calculation> it = calculations.iterator();
 		for (Integer count : summariesPoints) {
 			Calculation S = new CalculationImpl();
 			while (count-- > 0) {
 				Calculation c = r(it.next());
 				S.setFifoBase(o(S.getFifoBase()) + o(c.getFifoBase()));
 				S.setIncome(o(S.getIncome()) + o(c.getIncome()));
 				S.setCost(o(S.getCost()) + o(c.getCost()));
 			}
 			summaries.add(r(S));
 		}
 	}
 
 	private double o(Double v) {
 		if (v == null) {
 			return 0.0;
 		} else {
 			return v;
 		}
 	}
 
 	public static Double r(Double v) {
 		if (v == null) {
 			return null;
 		}
 		return Math.round(v * 1e2) / 1e2;
 	}
 
 	public static Double q(Double v) {
 		if (v == null) {
 			return null;
 		}
 		return Math.round(v * 1e4) / 1e4;
 	}
 
 	public static Calculation r(Calculation c) {
 		c.setFifoBase(r(c.getFifoBase()));
 		c.setIncome(r(c.getIncome()));
 		c.setCost(r(c.getCost()));
 		return c;
 	}
 
 	private void calculate(Currency currency) {
 		List<Point> plus = new ArrayList<Point>();
 		List<Point> minus = new ArrayList<Point>();
 		if (currency != baseCurrency) {
 			double SP = 0.0;
 			double SM = 0.0;
 			for (int i = 0; i < calculations.size(); i++) {
 				Calculation calculation = calculations.get(i);
 				if (calculation.getValue() != null
 						&& calculation.getCurrency() == currency) {
 					Double value = r(calculation.getValue());
 					Double signum = Math.signum(value);
 					if (value >= 0 && signum > 0) {
 						SP += +value;
 						plus.add(new Point(i, value));
 					} else if (value < 0 && signum < 0) {
 						SM += -value;
 						minus.add(new Point(i, -value));
 					}
 				}
 			}
 			double D = r(SP - SM);
 			if (D < 0) {
 				// add virtual point
 				plus.add(new Point(plus.size(), -D));
 			}
 			double sp = 0.0;
 			double sm = 0.0;
 			while (!plus.isEmpty() && !minus.isEmpty()) {
 				while (!plus.isEmpty() && plus.get(0).v <= 0) {
 					plus.remove(0);
 				}
 				while (!minus.isEmpty() && minus.get(0).v <= 0) {
 					minus.remove(0);
 				}
 				if (!plus.isEmpty() && !minus.isEmpty()) {
 					sp += plus.get(0).v;
 					sm += minus.get(0).v;
 					double v = r(Math.min(plus.get(0).v, minus.get(0).v));
 					plus.get(0).v = r(plus.get(0).v - v);
 					minus.get(0).v = r(minus.get(0).v - v);
 					double r = 0.0;
 					boolean d = r(sp - sm) >= 0;
 					if (d) {
 						r = calculations.get(plus.get(0).i).getExchange();
 					} else {
 						r = calculations.get(minus.get(0).i).getExchange();
 					}
 					Edge edge = new Edge(plus.get(0).i, minus.get(0).i, v, r, d);
 					edges.get(currency).add(edge);
 				}
 			}
			if (!plus.isEmpty() || minus.isEmpty()) {
 				throw new IllegalStateException("(PM) Calculator is wrong!");
 			}
 			System.out.println(currency + ": " + plus.isEmpty() + " "
 					+ minus.isEmpty());
 		}
 	}
 }
