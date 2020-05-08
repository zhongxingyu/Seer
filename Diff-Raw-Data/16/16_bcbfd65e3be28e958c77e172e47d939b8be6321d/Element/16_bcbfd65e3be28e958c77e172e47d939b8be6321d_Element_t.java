 package it.giacomos.android.osmer.rainAlert.gridAlgo;
 
 import java.util.ArrayList;
 
 import it.giacomos.android.osmer.rainAlert.interfaces.ImgParamsInterface;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.util.Log;
 
 public class Element {
 
 	public Index index;
 	public double xend = 0.0, xstart = 0.0, ystart = 0.0, yend = 0.0;
 	public float dbz;
 	private boolean increased;
 	
 	public ArrayList<ContiguousElementData> contiguousElementDataList; /* contiguousElementData */
 	
	public Element(int nrows, int ncols) 
	{
		/* if calculateDbz is not called (null bitmap), init dbz to 0.0 */
		dbz = 0.0f; 
 		index = new Index(nrows, ncols);
 		contiguousElementDataList = new ArrayList<ContiguousElementData>();
 	}
 
 	/** Calculates the average value of dbz of this element.
 	 * 
 	 * @param image the bitmap to analyze 
 	 * @param xc
 	 * @param yc
 	 * @param imgParams a specific implementation of ImgParamsInterface representing the 
 	 *        color map that associates dbz to colors. For instance MeteoFVGParams, that
 	 *        uses tones from violet to green to yellow to red to brown.
 	 *        
 	 * This method calculates the value of dbz inside the region of interest enclosed inside this Element.
 	 *        
 	 */
	public void calculateDbz(Bitmap image,	ImgParamsInterface imgParams) 
 	{
 		int argb = 0;
 		
 		int startX = (int) Math.floor(this.xstart);
 		int endX   = (int) Math.ceil(this.xend);
 		
 		int startY = (int) Math.floor(this.ystart);
 		int endY   = (int) Math.ceil(this.yend);
 		
 		int nr = endX - startX; 
 		int nc = endY - startY;
 		int npix = nr * nc;
 		
 		this.dbz = 0;
 		
 		for(int r = startX; r < endX; r++)
 		{
 			for(int c = startY; c < endY; c++)
 			{
 				argb = image.getPixel(r, c);
 				int [] arr_rgb = {Color.red(argb), Color.green(argb), Color.blue(argb)};
 				this.dbz += imgParams.getDbzForColor(arr_rgb);
 			}
 		}
 		
 		/* normalize dBZ */
 		this.dbz /= npix;
 		
 	//	Log.e("Element.calculateDbz", "index [ " + index.i  + ", " + index.j + "] "  + " [" + startX +"," + startY + ", " + endX + "," + endY  + "], dbz " + dbz);
 		
 		/* Functions drawing on image would follow. See img_overlay_grid.php, Element class.
 		 * 
 		 */
 	}
 
 	public void setHasIncreased(boolean has)
 	{
 		increased = has;
 	}
 	
 	public boolean hasIncreased()
 	{
 		return increased;
 	}
 	
 	public void initFromString(String section) 
 	{
 		int commaCnt = section.length() - section.replace(",", "").length();
 		if(commaCnt > 1) /* at least one contiguous Element to look for */
 		{
 			String [] contigs = section.split(";");
 			for(String contig : contigs)
 			{
 				String [] parts = contig.split(",");
 				if(parts.length > 2)
 				{
 					this.addContiguousData(new ContiguousElementData(Integer.parseInt(parts[0]),
 							Integer.parseInt(parts[1]), Double.parseDouble(parts[2])));
 				}
 			}
 		}
 	}
 
 	private void addContiguousData(ContiguousElementData contiguousElementData) {
 		if(contiguousElementData.isValid())
 			this.contiguousElementDataList.add(contiguousElementData);
 		else
 			Log.e("Element.addContiguousData","Element.addContiguousData: element not valid");
 	}
 
 	public ArrayList<ContiguousElementData> getLinks() 
 	{
 		return this.contiguousElementDataList;
 	}
 	
 	
 
 }
