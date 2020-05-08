 package com.abstracttech.ichiban.views.graphs;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 import com.abstracttech.ichiban.activities.IchibanActivity;
 import com.abstracttech.ichiban.data.Data;
 
 public class SpeedGraph extends ImageView {
 	
 	private Queue data;					// Stack of data to be drawn
 	
 	public SpeedGraph(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		data = new LinkedList();
 		Data.subscribe(this);// 565 340
 		
 		// TODO Auto-generated constructor stub
 	}
 	
 	protected float GetData()
 	{
		return Data.speedData.getSpeed()/Data._MAX_SPEED;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		super.onDraw(canvas);
 		
 		Paint p = new Paint();
 		p.setStrokeWidth(1);
 		
 		p.setColor(Color.BLACK);								//name
 		canvas.drawText("v(t)", this.getWidth() - 20, 20, p);
 		
 		p.setColor(Color.argb(255, 100, 100, 100));
 		
 		
 		for(int i = 0; i < 4; i++)		//side data: lines, percentage,... 
 		{
 			canvas.drawText("" + (i + 1) * 20 + "%", 2, this.getHeight() - (i + 1) * this.getHeight() / 4 - 2, p);
 			canvas.drawText("" + (i + 1) * 0.5f + "s", (i + 1) * this.getWidth() / 4, this.getHeight() - 6, p);
 			canvas.drawLine(0, (i + 1) * this.getHeight() / 4, this.getWidth(), (i + 1) * this.getHeight() / 4, p);
 			canvas.drawLine((i + 1) * this.getWidth() / 4, 0, (i + 1) * this.getWidth() / 4, this.getHeight(), p);
 		}
 		
 		if(!data.isEmpty() || GetData() != 0)
 		{
 			data.add(GetData());												//updating data from predefined source
 			if (data.size() > 3000 / (float)IchibanActivity._UPDATE_INTERVAL)		//to store only 20 seconds of data
 				data.poll();
 		}
 		
 		if(!data.isEmpty())
 		{					
 							
 			float[] points = new float[data.size() * 4];			//array of points of graph
 			float[] pointsShadow = new float[data.size() * 4];		//shadow array
 			
 			for (int i = 0; i < data.size() - 1; i++)		//Set points to draw lines
 			{
 				points[i * 4] = this.getWidth() * i / (float)data.size();
 				points[i * 4 + 1] = this.getHeight() * (1 - Float.parseFloat(data.toArray()[i].toString()));
 				points[i * 4 + 2] = this.getWidth() * (i + 1) / (float)data.size();
 				points[i * 4 + 3] = this.getHeight() * (1 - Float.parseFloat(data.toArray()[i + 1].toString()));
 				
 				pointsShadow[i * 4] = this.getWidth() * i / (float)data.size() + 5;
 				pointsShadow[i * 4 + 1] = this.getHeight() * (1 - Float.parseFloat(data.toArray()[i].toString())) + 5;
 				pointsShadow[i * 4 + 2] = this.getWidth() * (i + 1) / (float)data.size() + 5;
 				pointsShadow[i * 4 + 3] = this.getHeight() * (1 - Float.parseFloat(data.toArray()[i+1].toString())) + 5;
 			}
 			
 			canvas.drawLines(pointsShadow, p);
 			
 			p.setColor(Color.RED);							//setting paint (Color, stroke...) and drawing the lines
 			p.setStrokeWidth(4);
 			canvas.drawLines(points, p);
 		}	
 	}
 }
