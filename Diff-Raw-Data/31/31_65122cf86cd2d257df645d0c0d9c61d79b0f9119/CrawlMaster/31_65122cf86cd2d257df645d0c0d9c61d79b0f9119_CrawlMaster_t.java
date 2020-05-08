 package bot;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class CrawlMaster{
 public CrawlMaster(String startingAddress){
 try{start=new URL(startingAddress);}catch(Exception h){h.printStackTrace();}
 urlList=new ArrayList();
 }
 
 private URL start;
 private List urlList;
 private static final String HREF="href=\"", JOINTAG="#j";
 
 private boolean joinURL;
 
 public void startTrace(){
 //connect(), go into loop and put url into urlList, search the page for more url and add them into list
 
 }
 
 public void test() throws IOException{
 BufferedReader in=new BufferedReader(new InputStreamReader(start.openStream()));
 BufferedReader stdin=new BufferedReader(new InputStreamReader(System.in));
 String lineReadBuffer=""; System.out.println("<param>");
 String param=stdin.readLine();
 while((lineReadBuffer=in.readLine())!=null){pullURL(lineReadBuffer,param);}
 in.close();
 printList(urlList);
 System.out.println("Filter using?<key1>;<key2>;...");
String filList=stdin.readLine();
String keyList[]=filList.split(";");
List fg=filter(keyList,urlList);

 printList(fg);
 System.out.println("Download? [y]"); String opt="";
 opt=stdin.readLine();
 if(opt.equals("y")){
 System.out.println("<path>?");
 opt=stdin.readLine();
 System.out.println("Additional commands?");
 String adcom=stdin.readLine();
 if(adcom.indexOf(JOINTAG)!=-1){joinURL=true;}
 long mdl=download(fg,opt);
 System.out.println("Done. Downloaded "+mdl+" B");
 }

 }
 

 
 private List filter(String s, List x){
 List li=new ArrayList();
 for(int i=0;i<x.size();i++){
 String d=(String)x.get(i);
 if(d.indexOf(s)!=-1){
 li.add(d);}
 }
 return li;
 }
 
 private List filter(String s[], List x){
 List p=x,q=null;
 for(int i=0;i<s.length;i++){
 q=filter(s[i],p);
 p=q;
 }
 return q;
 }
 
 public void printList(List u){
 System.out.println("------------");
 for(int i=0;i<u.size();i++){
 System.out.println((String)u.get(i));
 }
 }
 
 private long getLength(URL x) throws IOException{
 return x.openConnection().getContentLength();
 }
 
 private boolean canDot(long downed, long tot){
 double r=(double)downed/(double)tot;
 r*=100;
 long f=(long)r;
 long k=f%10;
 f-=k;
 if(f%10==0 && f%2==0){return true;}
 return false;
 }
 
 private void pullURL(String s, String param){
 //go into an indefinite loop
 String sub;
 while(true){
 int k=s.indexOf(param);
 if(k==-1) break;
 ///////////////////////////
 sub=s.substring(k+param.length()); //start point of url
 int l=sub.indexOf("\"");
 urlList.add(sub.substring(0,l));
 s=new String(sub);
 }
 }
 
 private String getExtension(String s){
 int f=s.lastIndexOf("/");
 String k=s.substring(f+1);
 return k;
 }
 
 private String expandURL(String parent, String partialURL){

 if(joinURL==true){return parent+partialURL;}
 else return partialURL;
 }
 
 private long download(List s, String path) throws IOException{
 long master=0;
 for(int i=0;i<s.size();i++){
 String urlSt=expandURL(start.getFile(),(String)(s.get(i)));
 URL dlFile=new URL(urlSt);

 DataInputStream fileIn=new DataInputStream(new BufferedInputStream(dlFile.openStream()));
 DataOutputStream outFile=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path+getExtension(urlSt))));
 long tot=getLength(dlFile);
 long downed=0;
 System.out.print("..Downloading "+(i+1)+" of "+s.size()+" files; ");
 System.out.print("size "+tot+" B");
 System.out.flush();
 while(true){
 try{
 outFile.writeByte(fileIn.readByte());
 downed++;

 }catch(EOFException eof){
 fileIn.close();
 outFile.close();
 System.out.println("#");
 master+=tot;
 break;}
 }
 
 }
 return master;
 }
 
 
 public static void main(String[] rags) throws Exception{
 System.out.print("Address?");
 CrawlMaster cl=new CrawlMaster(new BufferedReader((new InputStreamReader(System.in))).readLine());
 cl.test(); //successful
 }
 }
 
