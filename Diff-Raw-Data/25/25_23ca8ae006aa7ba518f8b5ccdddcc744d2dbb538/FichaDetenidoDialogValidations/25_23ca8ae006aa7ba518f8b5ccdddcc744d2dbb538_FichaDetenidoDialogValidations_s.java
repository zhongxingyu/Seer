 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ve.com.fsjv.devsicodetv.controladores.eventos;
 
 import java.util.ArrayList;
 import javax.swing.JFrame;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.ConstantesApp;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.Procesos;
 import ve.com.fsjv.devsicodetv.vistas.FichaDetenidoDialog;
 
 /**
  *
  * @author FAMILIA-SJ
  */
 public class FichaDetenidoDialogValidations {
     private FichaDetenidoDialog formulario;
     private Procesos procesos;
     
     public FichaDetenidoDialogValidations(FichaDetenidoDialog formulario, Procesos procesos){
         this.formulario = formulario;
         this.procesos = procesos;
     }
     
     public ArrayList<String> validarCampos(){
         ArrayList<String> listaErrores = new ArrayList<String>();
         //arreglo de resultado de la validacion
         String[] arv;
         String resultado = ConstantesApp.INICIALIZAR_STRING;
         resultado = this.procesos.validarCampo(this.formulario.getTxtCodigoInterno(), ConstantesApp.TIPO_VALIDACION_VACIO, "Codigo Interno", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
        /*
        resultado = this.procesos.validarCampo(this.formulario.getTxtCedulaIdentidad(), ConstantesApp.TIPO_VALIDACION_VACIO, "Cedula de Identidad", ConstantesApp.CAMPO_NORMAL);
        if(resultado != null){
            listaErrores.add(resultado);
        }*/
         
         if(!this.formulario.getTxtCedulaIdentidad().getText().trim().equals("")){
             resultado = this.procesos.validarLengthCampo(this.formulario.getTxtCedulaIdentidad(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_CEDULA, "Cedula de Identidad", ConstantesApp.CAMPO_NORMAL);
             if(resultado != null){
                 listaErrores.add(resultado);
             }
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getTxtNombres(), ConstantesApp.TIPO_VALIDACION_VACIO, "Nombres", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         if(!this.formulario.getTxtNombres().getText().trim().equals("")){
             resultado = this.procesos.validarLengthCampo(this.formulario.getTxtNombres(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Nombres", ConstantesApp.CAMPO_REQUERIDO);
             if(resultado != null){
                 listaErrores.add(resultado);
             }
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getTxtApellidos(), ConstantesApp.TIPO_VALIDACION_VACIO, "Apellidos", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         if(!this.formulario.getTxtApellidos().getText().trim().equals("")){
             resultado = this.procesos.validarLengthCampo(this.formulario.getTxtApellidos(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Apellidos", ConstantesApp.CAMPO_REQUERIDO);
             if(resultado != null){
                 listaErrores.add(resultado);
             }
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getTxtLugarNacimiento(), ConstantesApp.TIPO_VALIDACION_VACIO, "Lugar de Nacimiento", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         if(!this.formulario.getTxtLugarNacimiento().getText().trim().equals("")){
             resultado = this.procesos.validarLengthCampo(this.formulario.getTxtLugarNacimiento(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Lugar Nacimiento", ConstantesApp.CAMPO_REQUERIDO);
             if(resultado != null){
                 listaErrores.add(resultado);
             }
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getTxtEdad(), ConstantesApp.TIPO_VALIDACION_VACIO, "Edad", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         resultado = this.procesos.validarCampo(this.formulario.getTxtEstatura(), ConstantesApp.TIPO_VALIDACION_DECIMAL, "Estatura", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbSexo(), ConstantesApp.TIPO_VALIDACION_VACIO, "Sexo", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbNacionalidad(), ConstantesApp.TIPO_VALIDACION_VACIO, "Nacionalidad", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbEstadoCivil(), ConstantesApp.TIPO_VALIDACION_VACIO, "Estado Civil", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbColorOjos(), ConstantesApp.TIPO_VALIDACION_VACIO, "Color de Ojos", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbColorPiel(), ConstantesApp.TIPO_VALIDACION_VACIO, "Color de Piel", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbColorCabello(), ConstantesApp.TIPO_VALIDACION_VACIO, "Color de Cabello", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbTipoNariz(), ConstantesApp.TIPO_VALIDACION_VACIO, "Tipo de Nariz", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbContextura(), ConstantesApp.TIPO_VALIDACION_VACIO, "Contextura", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbAnio(), ConstantesApp.TIPO_VALIDACION_VACIO, "Año (Fecha de Nacimiento)", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbMes(), ConstantesApp.TIPO_VALIDACION_VACIO, "Mes (Fecha de Nacimiento)", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getCmbDia(), ConstantesApp.TIPO_VALIDACION_VACIO, "Dia (Fecha de Nacimiento)", ConstantesApp.CAMPO_REQUERIDO);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarLengthCampo(this.formulario.getTxtAlias(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Alias", ConstantesApp.CAMPO_NORMAL);
             if(resultado != null){
                 listaErrores.add(resultado);
             }
         
         resultado = this.procesos.validarLengthCampo(this.formulario.getTxtPasaporte(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Pasaporte", ConstantesApp.CAMPO_NORMAL);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarLengthCampo(this.formulario.getTxtReligion(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Religion", ConstantesApp.CAMPO_NORMAL);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         
         resultado = this.procesos.validarLengthCampo(this.formulario.getTxtDocumentoAnterior(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Docu. Anterior", ConstantesApp.CAMPO_NORMAL);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarLengthCampo(this.formulario.getTxtCicatriz(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Cicatriz", ConstantesApp.CAMPO_NORMAL);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarLengthCampo(this.formulario.getTxtSeniasParticulares(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Señas Part.", ConstantesApp.CAMPO_NORMAL);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarLengthCampo(this.formulario.getTxtProfesion(), ConstantesApp.TIPO_VALIDACION_MAXLENGTH_TITULO, "Profesion", ConstantesApp.CAMPO_NORMAL);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getTxtTelefonoCelular(), ConstantesApp.TIPO_VALIDACION_TELEFONO, "Telefono Celular", ConstantesApp.CAMPO_NORMAL);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getTxtTelefonoEmergencia(), ConstantesApp.TIPO_VALIDACION_TELEFONO, "Telefono de Emergencia", ConstantesApp.CAMPO_NORMAL);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         resultado = this.procesos.validarCampo(this.formulario.getTxtTelefonoHabitacion(), ConstantesApp.TIPO_VALIDACION_TELEFONO, "Telefono de Habitacion", ConstantesApp.CAMPO_NORMAL);
         if(resultado != null){
             listaErrores.add(resultado);
         }
         
         return listaErrores;
     }
     
 }
