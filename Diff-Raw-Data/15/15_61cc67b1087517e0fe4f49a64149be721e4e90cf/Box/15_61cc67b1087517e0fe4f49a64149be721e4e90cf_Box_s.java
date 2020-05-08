 
 public class Box implements Pict{
 
	private double height;
	private double width;
 	private final char rim;
 	private final char content;
 	
 	public Box(char rim, char content, double width, double height) {
 		if(rim == ' '){
 			this.rim = '.';
 		}else{
 			this.rim = rim;
 		}
 		
 		this.content = content;
 		
 		if(width<=0){
 			this.width = 1;
 		}else{
 			this.width = width;
 		}
 		
 		if(height<=0){
 			this.height = 1;
 		}else{
 			this.height = height;
 		}
 	}
 
 	@Override
 	public void scale(double factor) {
 		if( factor >= 0.1 && factor <= 10.0){
 			height *= factor;
 			width *= factor;
 		}
 	}
 	
 	@Override
 	public String toString(){
 		StringBuilder sb = new StringBuilder();
 		//Top Boundary
 		for(double i = Math.ceil(width);i>0;i--){
 			sb.append(rim);
 		}
 		sb.append("\n");
 		if(height > 1){
 			//Content
 			for(double i = Math.ceil(height)-2; i>0; i--){
 				if(width > 1){
 					sb.append(rim);
 					for(double j = Math.ceil(width)-2;j>0;j--){
 						sb.append(content);
 					}
 					sb.append(rim+"\n");
 				}else{
 					sb.append(rim + "\n");
 				}
 			}
 			//Bottom Boundary
 			for(double i = Math.ceil(width);i>0;i--){
 				sb.append(rim);
 			}
 		}
 		
 		return sb.toString();
 	}
 
 }
