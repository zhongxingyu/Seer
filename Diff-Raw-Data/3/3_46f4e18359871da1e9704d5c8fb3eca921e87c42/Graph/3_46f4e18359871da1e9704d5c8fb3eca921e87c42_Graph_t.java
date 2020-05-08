 package com.abstracttech.ichiban.views.graphs;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 import com.abstracttech.ichiban.data.Data;
 import com.abstracttech.ichiban.data.StatisticData;
 
 public class Graph extends ImageView {
 	Object[] currentData;
 	protected float top,bottom;
 	protected StatisticData datasource;
 	protected boolean updateBorders=false;
 	private int typeID;
 	private GraphType currentType;
 
 	public Graph(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		Data.subscribe(this);// 565 340
 		int myid=getId();
 		if(Data.graphID[0]==myid)
 			typeID=0;
 		if(Data.graphID[1]==myid)
 			typeID=1;
 		if(Data.graphID[2]==myid)
 			typeID=2;
 		if(Data.graphID[3]==myid)
 			typeID=3;
 	}
 
 	/**
 	 * morphs the graph to selected type
 	 * @param type to morph into
 	 */
 	private void morph(GraphType type)
 	{
 		switch(type)
 		{
 		case PATH:
 			top=0;
 			bottom=0;
 			datasource=Data.pathData;
 			this.updateBorders=true;
 			break;
 		case SPEED:
 			bottom=-20;
 			top=160;
 			datasource=Data.speedData;
			this.updateBorders=false;
 			break;
 		case ACCELERATION:
 			bottom=-80;
 			top=80;
 			datasource=Data.accData;
			this.updateBorders=false;
 			break;
 		}
 		currentType=type;
 	}
 
 	protected float GetData()
 	{
 		return datasource.get();
 	}
 
 	protected boolean isThereData() {
 		return (datasource!=null && datasource.data!=null && Data.accData.data.size()>0);
 	}
 
 	protected Object[] getArray() {
 		return datasource.array;
 	}
 
 	private float getPoint(int i){
 		return 1 - ((Float)currentData[i] - bottom)/ (top-bottom);
 	}
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		//mighty morphin
 		if(currentType!=Data.graphs[typeID])
 			morph(Data.graphs[typeID]);
 
 		super.onDraw(canvas);
 		if(updateBorders)
 		{
 			top=Data.pathData.last;
 			bottom=Data.pathData.first;
 		}
 
 		Paint p = new Paint();
 		p.setStrokeWidth(1);
 
 		p.setColor(Color.BLACK);								//name
 		canvas.drawText("s(t)", this.getWidth() - 20, 20, p);
 
 		p.setColor(Color.argb(255, 100, 100, 100));
 
 
 		for(int i = 0; i < 4; i++)		//side data: lines, percentage,... 
 		{
 			canvas.drawText("" + (i + 1) * 20 + "%", 2, this.getHeight() - (i + 1) * this.getHeight() / 4 - 2, p);
 			canvas.drawText("" + (i + 1) * 0.5f + "s", (i + 1) * this.getWidth() / 4, this.getHeight() - 6, p);
 			canvas.drawLine(0, (i + 1) * this.getHeight() / 4, this.getWidth(), (i + 1) * this.getHeight() / 4, p);
 			canvas.drawLine((i + 1) * this.getWidth() / 4, 0, (i + 1) * this.getWidth() / 4, this.getHeight(), p);
 		}
 
 
 		if(isThereData())
 		{					
 			currentData= getArray();			
 			float[] points = new float[currentData.length * 4];			//array of points of graph
 			float[] pointsShadow = new float[currentData.length * 4];		//shadow array
 
 
 			for (int i = 0; i < currentData.length - 1; i++)		//Set points to draw lines
 			{
 				points[i * 4] = this.getWidth() * i / (float)currentData.length;
 				points[i * 4 + 1] = this.getHeight() * getPoint(i) - 2;
 				points[i * 4 + 2] = this.getWidth() * (i + 1) / (float)currentData.length;
 				points[i * 4 + 3] = this.getHeight() * getPoint(i+1) - 2;
 
 				pointsShadow[i * 4] = this.getWidth() * i / (float)currentData.length + 5;
 				pointsShadow[i * 4 + 1] = this.getHeight() * getPoint(i) + 5;
 				pointsShadow[i * 4 + 2] = this.getWidth() * (i + 1) / (float)currentData.length + 5;
 				pointsShadow[i * 4 + 3] = this.getHeight() * getPoint(i+1) + 5;
 			}
 
 			canvas.drawLines(pointsShadow, p);
 
 			p.setColor(Color.RED);							//setting paint (Color, stroke...) and drawing the lines
 			p.setStrokeWidth(4);
 			canvas.drawLines(points, p);
 		}	
 	}
 }
