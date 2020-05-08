 import java.util.Arrays;
 import java.util.Collections;
 
 public class Fast {
    public static void main(String[] args)
    {
        // rescale coordinates and turn on animation mode
        StdDraw.setXscale(0, 32768);
        StdDraw.setYscale(0, 32768);
        StdDraw.show(0);
      //input processing.
        String filename = args[0];
        In in = new In(filename);
        int N = in.readInt();
        Point[] points = new Point[N];
        for (int i = 0; i < N; i++) {
            int x = in.readInt();
            int y = in.readInt();
            Point p = new Point(x, y);
            points[i] = p;
            p.draw();
        }
        //
       
        
        //////////////////////////////////////////////////
        int searchToken;
        //Arrays.sort(points, Collections.reverseOrder());
        //Arrays.sort(points);
        for (int p = 0; p < N; p++)
        {
            Arrays.sort(points, p + 1, N, points[p].SLOPE_ORDER);
            //
            searchToken = 1;
            for (int i = 0; i < N; i++)
            {
                StdOut.println( "points["+i+"] "+points[i].toString()); 
            }
            for (int i = p + 2; i < N; ++i)
            {
                
               StdOut.println(points[p].toString() + "Checking..."+i+"|"+points[i] + "-" + points[i-1].toString());
                StdOut.println("Slope 1:"+points[p].slopeTo(points[i])+" Slope 2:"+points[p].slopeTo(points[i-1]));
                if (points[p].slopeTo(points[i]) 
                        == points[p].slopeTo(points[i-1]))
                {
                    searchToken++;
                    //StdOut.println("<-------"+searchToken);
                    if (i == (N-1))
                    {
                        if (searchToken >= 3)
                        {
                            // Construct buffer
                            Point[] outputBuffer = new Point[searchToken + 1];
                            outputBuffer[0] = points[p];
                            int outCount = searchToken;
                            while (searchToken > 0)
                            {
                                outputBuffer[searchToken] = points[i-searchToken];
                                searchToken--;
                            }
                            // sort
                            Arrays.sort(outputBuffer);
                            for (int o = 0; o < outCount; o++)
                            {
                                StdOut.print(outputBuffer[o].toString() + "->");
                            }
                            StdOut.print(
                       outputBuffer[outCount].toString() + "\n");                       
                            
                        }
                        searchToken = 1;
                    }
                }
                else
                {
                    //StdOut.println("------->"+searchToken);
                    if (searchToken >= 3)
                    {
                        // Construct buffer
                        Point[] outputBuffer = new Point[searchToken + 1];
                        outputBuffer[0] = points[p];
                        int outCount = searchToken;
                        while (searchToken > 0)
                        {
                            outputBuffer[searchToken] = points[i-searchToken];
                            searchToken--;
                        }
                        // sort
                        Arrays.sort(outputBuffer);
                        for (int o = 0; o < outCount; o++)
                        {
                            StdOut.print(outputBuffer[o].toString() + "->");
                        }
                        StdOut.print(
                    outputBuffer[outCount].toString() + "\n");
                       outputBuffer[0].drawTo(outputBuffer[searchToken]);
                        
                    }
                    searchToken = 1;
                }
            
            }
        }
        //////////////////////////////////////////////////
        // display to screen all at once
        StdDraw.show(0);
    }
 }
