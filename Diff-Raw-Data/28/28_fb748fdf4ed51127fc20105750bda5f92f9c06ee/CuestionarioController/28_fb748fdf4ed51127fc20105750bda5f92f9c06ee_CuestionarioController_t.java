 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import daoImpl.ContratoDaoImpl;
 import daoImpl.EmpresaCompletaDaoImpl;
 import internacionalizacionbeans.PreguntasInternacionalizacion;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 import javax.servlet.http.HttpServletRequest;
 import pojo.Contrato;
 import pojo.EmpresaCompleta;
 
 
 /**
  *
  * @author davidalmanza
  */
 @ManagedBean(name = "cuestionarioController")
 @ViewScoped
 public class CuestionarioController implements Serializable {
 
     private PreguntasInternacionalizacion pi = PreguntasInternacionalizacion.getinstancia();
     private List<String> p15Opciones, p18Opciones, p20Opciones, p21Opciones, p22Opciones, pc4Opciones, pc6Opciones, pc11Opciones,
             pc13Opciones, pc15Opciones, pd2Opciones, pd3Opciones, pd7Opciones, pd9Opciones, pd10Opciones, pd11Opciones, pd12Opciones,
             pd17Opciones, pd18Opciones, pd21Opciones, pe1Opciones, pe3Opciones, pe5Opciones, pe10Opciones = new ArrayList<String>();
    private String nombreComercial = "";
     private String d19i1, d19i2, d19i3, d19i4, d19i5, d19i6;
     private int anioExport;
     //para años del combobox
     private ArrayList<SelectItem> years = new ArrayList<SelectItem>();
     final int initialYear = 1900;
     final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
     private int[] preguntas = new int[2];
     //@ManagedProperty(value = "#{mbcompletado}")
     //private MBcompletadoInter completoInternac;
    
    private Boolean mostrarD8;
 
     public CuestionarioController() {
         pi.setB1(10);
         pi.setB2(10);
         pi.setB3(10);
         pi.setB4(10);
         pi.setB5(10);
         pi.setB6(10);
         pi.setB7(10);
         pi.setB8(10);
         pi.setB91(10);
         pi.setB92(1);
         pi.setB10(10);
         pi.setB11(10);
         pi.setB12(10);
         pi.setB13(9);
         pi.setB14(6);
         pi.setB16(3);
         pi.setB17(10);
         pi.setB19(5);
         pi.setC1(5);
         pi.setC2(10);
         pi.setC3(10);
         pi.setC5(10);
         pi.setC6(6);
         pi.setC7(10);
         pi.setC8(10);
         pi.setC9(5);
         pi.setC10(4);
         pi.setC12(3);
         pi.setC14(6);
         pi.setC16(9);
         pi.setC17(2);
         pi.setC18(2);
         pi.setC19(7);
         pi.setC20(10);
         pi.setC21(4);
         pi.setC22(3);
         pi.setC23(10);
         pi.setC24(10);
         pi.setC25(10);
         pi.setD1(5);
         pi.setD4(2);
         pi.setD5(2);
         pi.setD6(3);
         pi.setD8(2);
         pi.setD13(10);
         pi.setD14(10);
         pi.setD20(10);
         pi.setD25(4);
         pi.setD16(10);
         pi.setD19(3);
         pi.setD22(4);
         pi.setE2(10);
         pi.setE4(10);
         pi.setE6(10);
         pi.setE7(10);
         pi.setE8(10);
         pi.setE9(10);
         pi.setE11(10);
         pi.setE12(10);
         pi.setE13(10);
         pi.setE14(10);
         pi.setE15(10);
         pi.setE16(5);
         pi.setE17(10);
     }
 
     @PostConstruct
     public void init() {
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         pi.setId_contrato(Integer.parseInt(request.getParameter("idcontrato")));
 
         for (int year = initialYear; year <= currentYear; year++) {
             years.add(new SelectItem(year, String.valueOf(year)));
         }
 
         //Para cambiar el texto en la pregunta d19 segun lo elegido en A 12
         //int fa = 5;
         EmpresaCompleta empresa = new EmpresaCompleta();
         EmpresaCompletaDaoImpl empDao = new EmpresaCompletaDaoImpl();
 
         Contrato contrato = new Contrato();
         ContratoDaoImpl contraDao = new ContratoDaoImpl();
         contrato = contraDao.findById(pi.getId_contrato());
 
         empresa = empDao.findByIdContrato(contrato);
        nombreComercial = empresa.getRazonSocial();
         int fa = empresa.getFacturacionAnual();//completoInternac.getEc().getFacturacionAnual();
                 
         if (fa < 5) {
              d19i1 = "Menos de 200";
              d19i2 = "Entre 200 y 400";
              d19i3 = "Entre 400 y 1000";
              d19i4 = "Entre 1000 y 3,000";
              d19i5 = "Entre 3,000 y 8,000";
              d19i6 = "Más de 8000";
        } else if (fa >5 && fa < 8) {
             d19i1 = "Menos de 400";
              d19i2 = "Entre 400 y 1000";
              d19i3 = "Entre 1000 y 2,500";
              d19i4 = "Entre 2,500 y 6,000";
              d19i5 = "Entre 6,000 y 20,000";
              d19i6 = "Más de 20,000";
         } else {
             d19i1 = "Menos de 2,000";
             d19i2 = "Entre 2,000 y 4,000";
             d19i3 = "Entre 4,000 y 10,000";
             d19i4 = "Entre 10,000 y 30,000";
             d19i5 = "Entre 30,000 y 80,000";
             d19i6 = "Más de 80,000";
         }
     }
 
     public void sumarOpcion15() {
         Iterator<String> it = p15Opciones.iterator();
         int suma = 0;
         while (it.hasNext()) {
             suma += Integer.parseInt(it.next());
         }
         System.out.println(suma);
         pi.setB15(suma);
     }
 
      
     public void sumarOpcion18(){
         Iterator<String> it = p18Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());
         }
         System.out.println(suma);
         pi.setB18(suma);
     }
     
     public void sumarOpcion20(){
         Iterator<String> it = p20Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma >10){
             pi.setB20(10);
         }else{
             pi.setB20(suma);
         }
             
         
     }
     
     public void sumarOpcion21(){
         Iterator<String> it = p21Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma >10){
          pi.setB21(10);   
         }else{
          pi.setB21(suma);   
         }        
     }
     
     public void sumarOpcion22(){
         Iterator<String> it = p22Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma >10){
             pi.setB22(10);
         }else{
             pi.setB22(suma);
         }
         
     }
     
     public void calcularB23(){
        pi.setB23(6+pi.getA12()-pi.getA11_1());       
     }
     
     public void calcularB24(){
         int logica_evaluar = 5 + pi.getB5() - pi.getB6();
         
         if ( logica_evaluar >10){
             pi.setB24(10);
         }else if ( logica_evaluar < 0){
             pi.setB24(0);
         }else{
             pi.setB24(logica_evaluar);    
         }
     }
     
     public void sumarOpcionc4(){
         Iterator<String> it = pc4Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma > 10){
          pi.setC4(10);   
         }else{
             pi.setC4(suma);
         }
     }
     
     public void sumarOpcionc6(){
         Iterator<String> it = pc6Opciones.iterator();
         int suma =0,num=0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());
             num ++;
         }
         System.out.println(suma);
         
         if(num ==0 ){
             pi.setC6(0);
         }else{
             pi.setC6(suma/num);
         }
     }
     
     public void sumarOpcionc11(){
         Iterator<String> it = pc11Opciones.iterator();
         int suma =0,num=0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
             num ++;
         }
         System.out.println(suma);
         
             if(num == 0){
                pi.setC11(0);
             }else{
                if(((suma/2)+((suma/num)*1.5)) > 10){
                 pi.setC11(10);
                 }else{
                     pi.setC11(((suma/2)+((suma/num)*1.5))); 
                 }
             }
     }
  
     public void sumarOpcionc13(){
         Iterator<String> it = pc13Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());  
         }
         System.out.println(suma);
         pi.setC13(suma);
     }
     
     public void sumarOpcionc15(){
         Iterator<String> it = pc15Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma >10){
           pi.setC15(10);  
         }else{
           pi.setC15(suma);  
         }        
     }
     
     public void sumarOpciond2(){
         Iterator<String> it = pd2Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
        
         
         if(suma>10){
             pi.setD2(10);
         }else{
             pi.setD2(suma);
         }
         
     }
     
     public void sumarOpciond3(){
         Iterator<String> it = pd3Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if((suma + (pi.getD2() /2 )) > 10){
             pi.setD3(10);
         }else{
             pi.setD3(suma);
         }
       
     }
     
     public void sumarOpciond7(){
         Iterator<String> it = pd7Opciones.iterator();
         Double suma =0.0;
         while (it.hasNext()){
             suma += Double.parseDouble(it.next()); 
         }
         System.out.println(suma);
         pi.setD7(suma);
     }
     
     public void sumarOpciond9(){
         Iterator<String> it = pd9Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if( (5 + suma) < 0){
             pi.setD9(0);
         }else{
             pi.setD9(suma);
         }
     }
     
      public void sumarOpciond10(){
         Iterator<String> it = pd10Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma > 10){
             pi.setD10(10);
         }else{
             pi.setD10(suma);
         }
     }
      
      public void sumarOpciond11(){
         Iterator<String> it = pd11Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma > 10){
             pi.setD11(10);
         }else{
             pi.setD11(suma);
         }
     }
      
     public void sumarOpciond12(){
         Iterator<String> it = pd12Opciones.iterator();
         int suma =0,num=0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
             num ++;
         }
         System.out.println(suma);
         
         if(num ==0){
            pi.setD12(0); 
         }else{
            if((10 + ((suma + (suma/num)) / 2)) < 0){
              pi.setD12(0);
            } else{
                pi.setD12((10 + ((suma + (suma/num)) / 2)));
            }
         }
         pi.setD12(suma);
     }
     
     public void sumarOpciond17(){
         Iterator<String> it = pd17Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());  
         }
         System.out.println(suma);
         pi.setD17(suma);
     }
 
     public void sumarOpciond18(){
         Iterator<String> it = pd18Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());   
         }
         System.out.println(suma);
         pi.setD18(suma);
     }
     
     public void sumarOpciond21(){
         Iterator<String> it = pd21Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma > 10){
             pi.setD21(10);
         }else{
             pi.setD21(suma);
         }
     }
     
     public void sumarOpcione1(){
         Iterator<String> it = pe1Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());   
         }
         System.out.println(suma);
         pi.setE1(suma);
     }
    
     public void sumarOpcione3(){
         Iterator<String> it = pe3Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma > 10){
             pi.setE3(10);
         }else{
             pi.setE3(suma);
         }
     }
     
     public void sumarOpcione5(){
         Iterator<String> it = pe5Opciones.iterator();
         int suma =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
         }
         System.out.println(suma);
         
         if(suma > 10){
             pi.setE5(10);
         }else{
             pi.setE5(suma);
         }
     }
     
     public void sumarOpcione10(){
         Iterator<String> it = pe10Opciones.iterator();
         int suma =0,num =0;
         while (it.hasNext()){
             suma += Integer.parseInt(it.next());            
             num ++;
         }
         System.out.println(suma);
         
         if(num ==0){
             pi.setE10(0);
         }else{
             pi.setE10((suma /2) + ((suma/num) *1.5) );
         }
         pi.setE10(suma);
     }
     
     public String getNombreComercial() {
         return nombreComercial;
     }
     
     public List<String> getP15Opciones() {
         return p15Opciones;
     }
        
     public PreguntasInternacionalizacion getPi() {
         return pi;
     }
     
     public void setPi(PreguntasInternacionalizacion pi) {
         this.pi = pi;
     }
 
     public void setP15Opciones(List<String> p15Opciones) {
         this.p15Opciones = p15Opciones;
     }
 
     public List<String> getP18Opciones() {
         return p18Opciones;
     }
 
     public void setP18Opciones(List<String> p18Opciones) {
         this.p18Opciones = p18Opciones;
     }
 
     public List<String> getP21Opciones() {
         return p21Opciones;
     }
 
     public void setP21Opciones(List<String> p21Opciones) {
         this.p21Opciones = p21Opciones;
     }
 
     public List<String> getP20Opciones() {
         return p20Opciones;
     }
 
     public void setP20Opciones(List<String> p20Opciones) {
         this.p20Opciones = p20Opciones;
     }
 
     public List<String> getP22Opciones() {
         return p22Opciones;
     }
 
     public void setP22Opciones(List<String> p22Opciones) {
         this.p22Opciones = p22Opciones;
     }
 
     public int[] getPreguntas() {
         return preguntas;
     }
 
     public List<String> getPc4Opciones() {
         return pc4Opciones;
     }
 
     public void setPc4Opciones(List<String> pc4Opciones) {
         this.pc4Opciones = pc4Opciones;
     }
 
     public List<String> getPc6Opciones() {
         return pc6Opciones;
     }
 
     public void setPc6Opciones(List<String> pc6Opciones) {
         this.pc6Opciones = pc6Opciones;
     }
 
     public List<String> getPc11Opciones() {
         return pc11Opciones;
     }
 
     public void setPc11Opciones(List<String> pc11Opciones) {
         this.pc11Opciones = pc11Opciones;
     }
 
     public List<String> getPc13Opciones() {
         return pc13Opciones;
     }
 
     public void setPc13Opciones(List<String> pc13Opciones) {
         this.pc13Opciones = pc13Opciones;
     }
 
     public List<String> getPc15Opciones() {
         return pc15Opciones;
     }
 
     public void setPc15Opciones(List<String> pc15Opciones) {
         this.pc15Opciones = pc15Opciones;
     }
 
     public List<String> getPd2Opciones() {
         return pd2Opciones;
     }
 
     public void setPd2Opciones(List<String> pd2Opciones) {
         this.pd2Opciones = pd2Opciones;
     }
 
     public List<String> getPd3Opciones() {
         return pd3Opciones;
     }
 
     public void setPd3Opciones(List<String> pd3Opciones) {
         this.pd3Opciones = pd3Opciones;
     }
 
     public List<String> getPd7Opciones() {
         return pd7Opciones;
     }
 
     public void setPd7Opciones(List<String> pd7Opciones) {
         this.pd7Opciones = pd7Opciones;
     }
 
     public List<String> getPd9Opciones() {
         return pd9Opciones;
     }
 
     public void setPd9Opciones(List<String> pd9Opciones) {
         this.pd9Opciones = pd9Opciones;
     }
 
     public List<String> getPd10Opciones() {
         return pd10Opciones;
     }
 
     public void setPd10Opciones(List<String> pd10Opciones) {
         this.pd10Opciones = pd10Opciones;
     }
 
     public List<String> getPd11Opciones() {
         return pd11Opciones;
     }
 
     public void setPd11Opciones(List<String> pd11Opciones) {
         this.pd11Opciones = pd11Opciones;
     }
 
     public List<String> getPd12Opciones() {
         return pd12Opciones;
     }
 
     public void setPd12Opciones(List<String> pd12Opciones) {
         this.pd12Opciones = pd12Opciones;
     }
 
     public List<String> getPd17Opciones() {
         return pd17Opciones;
     }
 
     public void setPd17Opciones(List<String> pd17Opciones) {
         this.pd17Opciones = pd17Opciones;
     }
 
     public List<String> getPd18Opciones() {
         return pd18Opciones;
     }
 
     public void setPd18Opciones(List<String> pd18Opciones) {
         this.pd18Opciones = pd18Opciones;
     }
 
     public List<String> getPd21Opciones() {
         return pd21Opciones;
     }
 
     public void setPd21Opciones(List<String> pd21Opciones) {
         this.pd21Opciones = pd21Opciones;
     }
 
     public List<String> getPe1Opciones() {
         return pe1Opciones;
     }
 
     public void setPe1Opciones(List<String> pe1Opciones) {
         this.pe1Opciones = pe1Opciones;
     }
 
     public List<String> getPe3Opciones() {
         return pe3Opciones;
     }
 
     public void setPe3Opciones(List<String> pe3Opciones) {
         this.pe3Opciones = pe3Opciones;
     }
 
     public List<String> getPe10Opciones() {
         return pe10Opciones;
     }
 
     public void setPe10Opciones(List<String> pe10Opciones) {
         this.pe10Opciones = pe10Opciones;
     }
 
     public List<String> getPe5Opciones() {
         return pe5Opciones;
     }
 
     public void setPe5Opciones(List<String> pe5Opciones) {
         this.pe5Opciones = pe5Opciones;
     }
 
     public ArrayList<SelectItem> getYears() {
         return years;
     }
 
     public int getAnioExport() {
         return anioExport;
     }
 
     public void setAnioExport(int anioExport) {
         this.anioExport = anioExport;
     }
 
     public String getD19i1() {
         return d19i1;
     }
 
     public void setD19i1(String d19i1) {
         this.d19i1 = d19i1;
     }
 
     public String getD19i2() {
         return d19i2;
     }
 
     public String getD19i3() {
         return d19i3;
     }
 
     public String getD19i4() {
         return d19i4;
     }
 
     public String getD19i5() {
         return d19i5;
     }
 
     public String getD19i6() {
         return d19i6;
     }
    
    public Boolean getMostrarD8() {
        return mostrarD8;
    }

    public void setMostrarD8(Boolean mostrarD8) {
        this.mostrarD8 = mostrarD8;
    }
    
    public void renderizarD8(){
        if ( pi.getD6() <=  0 ){
            mostrarD8 = false;
        }else { 
            mostrarD8 = true;}
        System.out.print(mostrarD8);
    }
 
     /*public void setCompletoInternac(MBcompletadoInter completoInternac) {
         this.completoInternac = completoInternac;
     }
 
     public MBcompletadoInter getCompletoInternac() {
         return completoInternac;
     }*/
 
 }
