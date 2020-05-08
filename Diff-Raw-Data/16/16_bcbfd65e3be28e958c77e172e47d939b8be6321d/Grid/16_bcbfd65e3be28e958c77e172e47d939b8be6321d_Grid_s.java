 package it.giacomos.android.osmer.rainAlert.gridAlgo;
 
 import it.giacomos.android.osmer.rainAlert.interfaces.ImgParamsInterface;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.util.Log;
 
 public class Grid 
 {
 	public int nrows = 0, ncols = 0;
 	
 	private double xc = 0, yc = 0; /* center coordinates in the image of the radar */
 	
 	public double width = 0, height = 0; /* default osmer img size */
 	
 	public ArrayList<Element>elements; /* array of Element */
 	
 	/** calculates the dbz on the image provided
 	  */
 	public void calculateDbz(Bitmap image, ImgParamsInterface imgParams)
 	{
 		for(Element e : elements)
 		{
			e.calculateDbz(image, this.xc, this.yc, imgParams);
 		}
 	}
 	
 	public void setImgSize(int w, int h)
 	{
 		this.width = w;
 		this.height = h;
 	}
 	
 	public Element idxGet(Index idx)
 	{
 		return this.get(idx.i, idx.j);
 	}
 	
 	public Element get(int r, int c)
 	{
 	//	Log.e("Element.get", "r " + r + " c " + c);
 		if(r < this.nrows && c < this.ncols)
 		{
 			return this.elements.get(c + r * this.ncols);
 		}
 		return null;
 	}
 	
 	public double getCenterX()
 	{
 		return this.xc;
 	}
 	
 	public double getCenterY()
 	{
 		return this.yc;
 	}
 	
 	public void init(String config, double xc, double yc, double d, double e)
 	{
 		this.width = d;
 		this.height = e;
 		this.xc = xc;
 		this.yc = yc;
 		
 		if(this.width > 0 && this.height > 0 && xc >= 0.0 && yc >= 0.0)
 		{
 			
 			if(config.length() > 0)
 			{
 				/* clean spaces and tabs */
 				config = config.replace("\t", "");
 				config = config.replace(" ", "");
 				
 				String [] lines = config.split("\n");
 				
 				this.nrows = 0;
 				
 				this.elements = new ArrayList<Element>();
 				
 				for(String line : lines)
 				{
 					line = line.replace("\n", "");
 					int indexOf = line.indexOf('#');
 					
 					
 					if(indexOf >= 0) /* copy the part of string preceding '#' */
 						line = line.substring(0, indexOf);
 //					Log.e("Grid", "line: " + line);
 					
 					if(line.length() > 2)
 					{
 						/* sections */
 						String [] secs = line.split("\\|");
 						
 						this.ncols = 0;
 						
 						for(String section : secs)
 						{
 						///	if(!section.isEmpty())
 							{
 								/* Build a new Element with the i,j indexes inside the grid */
 								Element element = new Element(this.nrows, this.ncols);
 								/* setup dependencies for the element with their weight */
 								element.initFromString(section);
 								this.elements.add(element);
 								this.ncols = this.ncols + 1;
 							}
 							
 						}
 						this.nrows = this.nrows + 1;
 					}
 					
 				}
 				
 				/* width of a single  cell */
 				double xstep = this.width / this.ncols;
 				double ystep = this.height / this.nrows; /* height of a single cell */
 				
 				Element ele = null;
 				for(int r = 0; r < this.nrows; r++)
 				{
 					for(int c = 0; c < this.ncols; c++)
 					{
 						ele = this.get(r, c);
 						ele.xstart = xc - d/2 + c * xstep;
 						ele.xend = ele.xstart + xstep;
 						ele.ystart = yc - e/2 + r * ystep;
 						ele.yend = ele.ystart + ystep;
 					}
 				}
 			}
 			else
 				Log.e("Grid.init", "Error: file conffile has no contents");
 		}
 		else	
 			Log.e("Grid.init", "Error: image width and height must be set before calling init, with setImgSize");
 	}
 	
 //	public String toString()
 //	{
 //		String str = "";
 //		str +=  "<h4>Grid {this.nrows} x {this.ncols}</h4>";
 //		str +=  "<p>muovi il mouse sopra ogni cella per vedere i collegamenti tra le celle</p>";
 //		str +=  "<p>({this.xc} , {this.yc})   w: {this.width}   h: {this.height}</p>";
 //		str +=  "<table width=\"{this.height}\" height=\"{this.width}\" border=\"1\" >\n<tr>\n";
 //		
 //		i = 0;
 //		foreach(this.elements as e)
 //		{
 //			links = "";
 //			color = "white";
 //			
 //			if(e->hasLinks())
 //			{
 //				if(e->linksCount() == 1)
 //					color = "#eeffee";
 //				else if(e->linksCount() == 2)
 //					color = "#aaffaa";
 //				else	
 //					color = "#55ff55";
 //				contiguousList = e->indexes;
 //				foreach(contiguousList as contEl)
 //				{
 //					links += "{contEl->index->i}, {contEl->index->j}: {contEl->weight}\n";
 //				}
 //			}
 //			x1 = sprintf("%.1f",e->xstart);
 //			x2 = sprintf("%.1f",e->xend);
 //			y1 = sprintf("%.1f",e->ystart);
 //			y2 = sprintf("%.1f",e->yend);
 //			str +=  "<td title=\"links\" colspan=\"1\" style=\"background:color; font-size:8pt\">{e->index->i}, {e->index->j}\n" .
 //				"({x1}, {x2}),({y1},{y2}) </td>";
 //			
 //			i = i + 1;
 //			if(i % this.ncols == 0)	
 //				str +=  "</tr><tr>\n";
 //		}
 //		
 //		
 //		str .=  "</tr></table>\n";
 //		
 //		return str;
 //	}
 }
