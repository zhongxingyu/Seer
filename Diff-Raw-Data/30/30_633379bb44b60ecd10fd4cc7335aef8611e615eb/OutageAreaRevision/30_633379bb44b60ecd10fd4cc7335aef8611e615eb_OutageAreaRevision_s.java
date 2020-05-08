 package com.pocketcookies.pepco.model;
 
 public class OutageAreaRevision implements Comparable<OutageAreaRevision> {
 	private int id;
 	private OutageArea area;
 	private int customersOut;
 	// Associates us with a parser run. We also get the time at which the page
 	// was requested for free.
 	private ParserRun parserRun;
 
 	public OutageAreaRevision(OutageArea area, int customersOut,
 			final ParserRun parserRun) {
 		super();
 		setArea(area);
 		setCustomersOut(customersOut);
 		setParserRun(parserRun);
 	}
 
 	public OutageAreaRevision(final int id, final OutageArea area,
 			final int customersOut, final ParserRun parserRun) {
 		setId(id);
 		setArea(area);
 		setCustomersOut(customersOut);
 		setParserRun(parserRun);
 	}
 
 	public OutageAreaRevision() {
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public OutageArea getArea() {
 		return area;
 	}
 
 	public int getCustomersOut() {
 		return customersOut;
 	}
 
 	private void setId(int id) {
 		this.id = id;
 	}
 
 	private void setArea(OutageArea area) {
 		this.area = area;
 	}
 
 	private void setCustomersOut(int customersOut) {
 		this.customersOut = customersOut;
 	}
 
 	public ParserRun getParserRun() {
 		return parserRun;
 	}
 
 	private void setParserRun(ParserRun parserRun) {
 		this.parserRun = parserRun;
 	}
 
 	@Override
 	public int compareTo(OutageAreaRevision o) {
		final int timeComparison = (int) (this.getParserRun().getRunTime()
				.getTime() - o.getParserRun().getRunTime().getTime());
		// We only use the times for sorting. Since we're actually putting this
		// stuff in a TreeSet, we don't want to accidentally lose entries just
		// because they occur at the same time. Thus, we compare by the id to
		// check whether they are truly equal.
		if (timeComparison == 0)
 			return this.getId() - o.getId();
		return timeComparison;
 	}
 
 }
