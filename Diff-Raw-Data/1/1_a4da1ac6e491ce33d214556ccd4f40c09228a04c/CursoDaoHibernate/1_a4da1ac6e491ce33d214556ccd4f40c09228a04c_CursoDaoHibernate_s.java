 /*
  * The MIT License
  *
  * Copyright 2012 Universidad de Montemorelos A. C.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package mx.edu.um.academia.dao.impl;
 
 import com.liferay.portal.kernel.dao.orm.QueryUtil;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.model.User;
 import com.liferay.portal.service.UserLocalServiceUtil;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.portlet.documentlibrary.model.DLFileEntry;
 import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
 import com.liferay.portlet.journal.model.JournalArticle;
 import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
 import java.math.MathContext;
 import java.math.RoundingMode;
 import java.util.*;
 import mx.edu.um.academia.dao.CursoDao;
 import mx.edu.um.academia.dao.ExamenDao;
 import mx.edu.um.academia.model.*;
 import mx.edu.um.academia.utils.Constantes;
 import net.sf.jasperreports.engine.JasperReport;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.*;
 import static org.hibernate.type.StandardBasicTypes.STRING;
 import static org.hibernate.type.StandardBasicTypes.TIMESTAMP;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.support.ResourceBundleMessageSource;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author J. David Mendoza <jdmendoza@um.edu.mx>
  */
 @Repository
 @Transactional
 public class CursoDaoHibernate implements CursoDao {
 
     private static final Logger log = LoggerFactory.getLogger(CursoDaoHibernate.class);
     @Autowired
     private SessionFactory sessionFactory;
     @Autowired
     private ExamenDao examenDao;
     @Autowired
     private ResourceBundleMessageSource messages;
 
     public CursoDaoHibernate() {
         log.info("Nueva instancia del dao de cursos");
     }
 
     private Session currentSession() {
         return sessionFactory.getCurrentSession();
     }
 
     @Override
     public Map<String, Object> lista(Map<String, Object> params) {
         log.debug("Buscando lista de cursos con params {}", params);
         if (params == null) {
             params = new HashMap<>();
         }
 
         if (!params.containsKey("max") || params.get("max") == null) {
             params.put("max", 5);
         } else {
             params.put("max", Math.min((Integer) params.get("max"), 100));
         }
 
         if (params.containsKey("pagina") && params.get("pagina") != null) {
             Long pagina = (Long) params.get("pagina");
             Long offset = (pagina - 1) * (Integer) params.get("max");
             params.put("offset", offset.intValue());
         }
 
         if (!params.containsKey("offset") || params.get("offset") == null) {
             params.put("offset", 0);
         }
 
         Criteria criteria = currentSession().createCriteria(Curso.class);
         Criteria countCriteria = currentSession().createCriteria(Curso.class);
 
         if (params.containsKey("comunidades")) {
             criteria.add(Restrictions.in("comunidadId", (Set<Integer>) params.get("comunidades")));
             countCriteria.add(Restrictions.in("comunidadId", (Set<Integer>) params.get("comunidades")));
         }
 
         if (params.containsKey("filtro")) {
             String filtro = (String) params.get("filtro");
             Disjunction propiedades = Restrictions.disjunction();
             propiedades.add(Restrictions.ilike("codigo", filtro, MatchMode.ANYWHERE));
             propiedades.add(Restrictions.ilike("nombre", filtro, MatchMode.ANYWHERE));
             criteria.add(propiedades);
             countCriteria.add(propiedades);
         }
 
         if (params.containsKey("order")) {
             String campo = (String) params.get("order");
             if (params.get("sort").equals("desc")) {
                 criteria.addOrder(Order.desc(campo));
             } else {
                 criteria.addOrder(Order.asc(campo));
             }
         }
         criteria.addOrder(Order.desc("fechaModificacion"));
 
         criteria.setFirstResult((Integer) params.get("offset"));
         criteria.setMaxResults((Integer) params.get("max"));
         params.put("cursos", criteria.list());
 
         countCriteria.setProjection(Projections.rowCount());
         List cantidades = countCriteria.list();
         if (cantidades != null) {
             params.put("cantidad", (Long) cantidades.get(0));
         } else {
             params.put("cantidad", 0L);
         }
 
         return params;
     }
 
     @Override
     public Curso obtiene(Long cursoId) {
         log.debug("Obteniendo curso {}", cursoId);
         return (Curso) currentSession().get(Curso.class, cursoId);
     }
 
     @Override
     public Curso obtiene(String codigo, Long comunidadId) {
         log.debug("Obteniendo curso por codigo {} y comunidad {}", codigo, comunidadId);
         Query query = currentSession().createQuery("select c from Curso c where c.codigo = :codigo and c.comunidadId = :comunidadId");
         query.setString("codigo", codigo);
         query.setLong("comunidadId", comunidadId);
         return (Curso) query.uniqueResult();
     }
 
     @Override
     public Curso crea(Curso curso, User creador) {
         log.debug("Creando curso {} por usuario", curso, creador);
         Date fecha = new Date();
         curso.setFechaCreacion(fecha);
         curso.setFechaModificacion(fecha);
         if (creador != null) {
             curso.setCreador(creador.getScreenName());
         } else {
             curso.setCreador("admin");
         }
         currentSession().save(curso);
         currentSession().flush();
 
         Reporte reporte = curso.getReporte();
         if (reporte != null) {
             this.asignaReporte(reporte, curso);
         }
         currentSession().flush();
 
         return curso;
     }
 
     @Override
     public Curso actualiza(Curso otro, User creador) {
         log.debug("Actualizando curso {} por usuario {}", otro, creador);
         Curso curso = (Curso) currentSession().get(Curso.class, otro.getId());
         currentSession().refresh(curso);
         Long intro = curso.getIntro();
         log.debug("CursoIntro:", curso.getIntro());
         BeanUtils.copyProperties(otro, curso, new String[]{"id", "version", "fechaCreacion", "objetos", "intro", "correoId"});
         log.debug("CursoIntro:", curso.getIntro());
         curso.setIntro(intro);
         curso.setFechaModificacion(new Date());
         if (creador != null) {
             curso.setCreador(creador.getScreenName());
         } else {
             curso.setCreador("admin");
         }
         currentSession().update(curso);
         currentSession().flush();
 
         Reporte reporte = curso.getReporte();
         if (reporte != null) {
             this.modificaReporte(reporte, curso);
         }
         currentSession().flush();
         return curso;
     }
 
     @Override
     public void asignaIntro(Curso curso) {
         Query query = currentSession().createQuery("update Curso set intro = :intro where id = :id and version = :version");
         query.setLong("intro", curso.getIntro());
         query.setLong("id", curso.getId());
         query.setLong("version", curso.getVersion());
         query.executeUpdate();
     }
 
     @Override
     public String elimina(Long cursoId, User creador) {
         log.debug("Eliminando curso {} por usuario {}", cursoId, creador);
         // Dando de baja alumnos
         Query query = currentSession().createQuery("delete from AlumnoCurso where id.curso.id = :cursoId");
         query.setLong("cursoId", cursoId);
         query.executeUpdate();
 
         query = currentSession().createQuery("delete from Reporte where curso.id = :cursoId");
         query.setLong("cursoId", cursoId);
         query.executeUpdate();
 
         query = currentSession().createQuery("delete from PortletCurso where curso.id = :cursoId");
         query.setLong("cursoId", cursoId);
         query.executeUpdate();
 
         // Dando de baja los objetos
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         curso.getObjetos().clear();
         currentSession().update(curso);
 
         if (curso.getIntro() != null) {
             try {
                 JournalArticleLocalServiceUtil.deleteJournalArticle(curso.getIntro());
             } catch (PortalException | SystemException ex) {
                 log.error("No se pudo eliminar el articulo de introduccion", ex);
             }
         }
 
         if (curso.getCorreoId() != null) {
             try {
                 JournalArticleLocalServiceUtil.deleteJournalArticle(curso.getCorreoId());
             } catch (PortalException | SystemException ex) {
                 log.error("No se pudo eliminar el articulo de correo", ex);
             }
         }
 
         String nombre = curso.getNombre();
         currentSession().delete(curso);
         return nombre;
     }
 
     @Override
     public Map<String, Object> objetos(Long id, Set<Long> comunidades) {
         log.debug("Buscando los objetos del curso {}", id);
         Curso curso = (Curso) currentSession().get(Curso.class, id);
         List<ObjetoAprendizaje> objetos = curso.getObjetos();
         log.debug("Lista de seleccionados");
         for (ObjetoAprendizaje objeto : objetos) {
             log.debug("Seleccionado: " + objeto.getNombre());
         }
         Map<String, Object> resultado = new HashMap<>();
         resultado.put("seleccionados", objetos);
 
         Criteria criteria = currentSession().createCriteria(ObjetoAprendizaje.class);
         criteria.add(Restrictions.in("comunidadId", (Set<Long>) comunidades));
         criteria.addOrder(Order.asc("codigo"));
         log.debug("Lista de disponibles");
         List<ObjetoAprendizaje> disponibles = criteria.list();
         disponibles.removeAll(objetos);
         for (ObjetoAprendizaje objeto : disponibles) {
             log.debug("Disponible: " + objeto.getNombre());
         }
         resultado.put("disponibles", disponibles);
         log.debug("regresando {}", resultado);
         return resultado;
     }
 
     @Override
     public List<ObjetoAprendizaje> objetos(Long id) {
         log.debug("Buscando los objetos del curso {}", id);
         Curso curso = (Curso) currentSession().get(Curso.class, id);
         List<ObjetoAprendizaje> objetos = curso.getObjetos();
         for (ObjetoAprendizaje objeto : objetos) {
             log.debug("Seleccionado: " + objeto.getNombre());
         }
         return objetos;
     }
 
     @Override
     public void agregaObjetos(Long cursoId, Long[] objetosArray) {
         log.debug("Agregando objetos {} a curso {}", objetosArray, cursoId);
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         curso.getObjetos().clear();
         for (Long objetoId : objetosArray) {
             curso.getObjetos().add((ObjetoAprendizaje) currentSession().load(ObjetoAprendizaje.class, objetoId));
         }
         log.debug("Actualizando curso {}", curso);
         currentSession().update(curso);
         currentSession().flush();
     }
 
     @Override
     public Map<String, Object> verContenido(Long cursoId) {
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         List<ObjetoAprendizaje> objetos = curso.getObjetos();
         for (ObjetoAprendizaje objeto : objetos) {
             for (Contenido contenido : objeto.getContenidos()) {
                 log.debug("{} : {} : {}", new Object[]{curso.getCodigo(), objeto.getCodigo(), contenido.getCodigo()});
             }
         }
 
         Map<String, Object> resultado = new HashMap<>();
         resultado.put("objetos", objetos);
         return resultado;
     }
 
     @Override
     public List<Curso> todos(Set<Long> comunidades) {
         log.debug("Buscando lista de cursos en las comunidades {}", comunidades);
         Criteria criteria = currentSession().createCriteria(Curso.class);
 
         criteria.add(Restrictions.in("comunidadId", comunidades));
 
         criteria.addOrder(Order.desc("codigo"));
 
         return criteria.list();
     }
 
     @Override
     public PortletCurso guardaPortlet(Long cursoId, String portletId) {
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         PortletCurso portlet = (PortletCurso) currentSession().get(PortletCurso.class, portletId);
         if (portlet == null) {
             portlet = new PortletCurso(portletId, curso);
             currentSession().save(portlet);
         } else {
             portlet.setCurso(curso);
             currentSession().update(portlet);
         }
         return portlet;
     }
 
     @Override
     public PortletCurso obtienePortlet(String portletId) {
         return (PortletCurso) currentSession().get(PortletCurso.class, portletId);
     }
 
     @Override
     public Alumno obtieneAlumno(Long id) {
         return (Alumno) currentSession().get(Alumno.class, id);
     }
 
     @Override
     public void inscribe(Curso curso, Alumno alumno, Boolean creaUsuario, String estatus) {
         log.debug("Inscribiendo a alumno {} en curso {}", alumno, curso);
         if (creaUsuario) {
             log.debug("Creando alumno primero");
             alumno.setComunidad(curso.getComunidadId());
             currentSession().save(alumno);
         }
 
         log.debug("Inscribiendo...");
         AlumnoCursoPK pk = new AlumnoCursoPK(alumno, curso);
         AlumnoCurso alumnoCurso = (AlumnoCurso) currentSession().get(AlumnoCurso.class, pk);
         if (alumnoCurso == null) {
             alumnoCurso = new AlumnoCurso(alumno, curso, estatus);
             currentSession().save(alumnoCurso);
         } else {
             alumnoCurso.setEstatus(estatus);
             currentSession().update(alumnoCurso);
         }
         currentSession().flush();
     }
 
     @Override
     public Boolean estaInscrito(Long cursoId, Long alumnoId) {
         log.debug("Validando si el alumno {} esta inscrito en {}", alumnoId, cursoId);
         Curso curso = (Curso) currentSession().load(Curso.class, cursoId);
         Alumno alumno = (Alumno) currentSession().load(Alumno.class, alumnoId);
         AlumnoCursoPK pk = new AlumnoCursoPK(alumno, curso);
         AlumnoCurso alumnoCurso = (AlumnoCurso) currentSession().get(AlumnoCurso.class, pk);
         boolean resultado = false;
         if (alumnoCurso != null && (Constantes.INSCRITO.equals(alumnoCurso.getEstatus()) || Constantes.CONCLUIDO.equals(alumnoCurso.getEstatus()))) {
             resultado = true;
         }
         return resultado;
     }
 
     @Override
     public List<ObjetoAprendizaje> objetosAlumno(Long cursoId, Long alumnoId, ThemeDisplay themeDisplay) {
         log.debug("Obteniendo objetos de aprendizaje del curso {} para el alumno {}", cursoId, alumnoId);
 
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         log.debug("{}", curso);
         Alumno alumno = (Alumno) currentSession().load(Alumno.class, alumnoId);
         log.debug("{}", alumno);
         AlumnoCursoPK alumnoCursoPK = new AlumnoCursoPK(alumno, curso);
         AlumnoCurso alumnoCurso = (AlumnoCurso) currentSession().get(AlumnoCurso.class, alumnoCursoPK);
         alumnoCurso.setUltimoAcceso(new Date());
         currentSession().update(alumnoCurso);
         currentSession().flush();
 
         List<ObjetoAprendizaje> objetos = curso.getObjetos();
         boolean noAsignado = true;
         boolean activo = false;
         Date fecha = new Date();
         for (ObjetoAprendizaje objeto : objetos) {
             boolean bandera = true;
             AlumnoObjetoAprendizajePK pk2 = new AlumnoObjetoAprendizajePK(alumno, objeto);
             AlumnoObjetoAprendizaje alumnoObjeto = (AlumnoObjetoAprendizaje) currentSession().get(AlumnoObjetoAprendizaje.class, pk2);
             if (alumnoObjeto == null) {
                 alumnoObjeto = new AlumnoObjetoAprendizaje(alumno, objeto);
                 currentSession().save(alumnoObjeto);
                 currentSession().flush();
             }
             for (Contenido contenido : objeto.getContenidos()) {
                 log.debug("Cargando contenido {} del objeto {} : activo : {}", new Object[]{contenido, objeto, contenido.getActivo()});
                 AlumnoContenidoPK pk = new AlumnoContenidoPK(alumno, contenido);
                 AlumnoContenido alumnoContenido = (AlumnoContenido) currentSession().get(AlumnoContenido.class, pk);
                 if (alumnoContenido == null) {
                     alumnoContenido = new AlumnoContenido(alumno, contenido);
                     currentSession().save(alumnoContenido);
                     currentSession().flush();
                 }
                 log.debug("Buscando {} : {}", bandera, alumnoContenido.getTerminado());
                 if (bandera && alumnoContenido.getTerminado() == null && !activo) {
                     this.asignaContenido(cursoId, alumnoContenido, contenido, themeDisplay);
                     log.debug("Activando a {}", contenido.getNombre());
                     contenido.setActivo(bandera);
                     activo = true;
                     alumnoContenido.setIniciado(fecha);
                     currentSession().update(alumnoContenido);
 
                     if (alumnoObjeto.getIniciado() == null) {
                         alumnoObjeto.setIniciado(fecha);
                         currentSession().update(alumnoObjeto);
                     }
 
                     currentSession().flush();
                     bandera = false;
                     noAsignado = false;
                 }
                 log.debug("Asignando el contenido {} : activo : {}", contenido.getNombre(), contenido.getActivo());
                 contenido.setAlumno(alumnoContenido);
             }
         }
         if (noAsignado) {
             log.debug("No asignado >> asignando");
             for (ObjetoAprendizaje objeto : objetos) {
                 boolean bandera = true;
                 AlumnoObjetoAprendizajePK pk2 = new AlumnoObjetoAprendizajePK(alumno, objeto);
                 AlumnoObjetoAprendizaje alumnoObjeto = (AlumnoObjetoAprendizaje) currentSession().get(AlumnoObjetoAprendizaje.class, pk2);
                 if (alumnoObjeto == null) {
                     alumnoObjeto = new AlumnoObjetoAprendizaje(alumno, objeto);
                     currentSession().save(alumnoObjeto);
                     currentSession().flush();
                 }
                 for (Contenido contenido : objeto.getContenidos()) {
                     log.debug("Cargando contenido {} del objeto {}", contenido, objeto);
                     AlumnoContenidoPK pk = new AlumnoContenidoPK(alumno, contenido);
                     AlumnoContenido alumnoContenido = (AlumnoContenido) currentSession().get(AlumnoContenido.class, pk);
                     if (alumnoContenido == null) {
                         alumnoContenido = new AlumnoContenido(alumno, contenido);
                         currentSession().save(alumnoContenido);
                         currentSession().flush();
                     }
                     if (bandera && !activo) {
                         this.asignaContenido(cursoId, alumnoContenido, contenido, themeDisplay);
                         log.debug("Activando a {}", contenido.getNombre());
                         contenido.setActivo(true);
                         activo = true;
                         alumnoContenido.setIniciado(fecha);
                         currentSession().update(alumnoContenido);
 
                         if (alumnoObjeto.getIniciado() == null) {
                             alumnoObjeto.setIniciado(fecha);
                             currentSession().update(alumnoObjeto);
                         }
 
                         currentSession().flush();
                         bandera = false;
                     }
                     contenido.setAlumno(alumnoContenido);
                 }
             }
         }
         return objetos;
     }
 
     @Override
     public List<ObjetoAprendizaje> objetosAlumno(Long cursoId, Long contenidoId, Long alumnoId, ThemeDisplay themeDisplay) {
         log.debug("Obteniendo objetos de aprendizaje del curso {} para el alumno {}", cursoId, alumnoId);
 
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         log.debug("{}", curso);
         Alumno alumno = (Alumno) currentSession().load(Alumno.class, alumnoId);
         log.debug("{}", alumno);
         AlumnoCursoPK alumnoCursoPK = new AlumnoCursoPK(alumno, curso);
         AlumnoCurso alumnoCurso = (AlumnoCurso) currentSession().get(AlumnoCurso.class, alumnoCursoPK);
         alumnoCurso.setUltimoAcceso(new Date());
         currentSession().update(alumnoCurso);
         currentSession().flush();
 
         List<ObjetoAprendizaje> objetos = curso.getObjetos();
         boolean terminado = true;
         boolean noAsignado = true;
         boolean activo = false;
         Date fecha = new Date();
         for (ObjetoAprendizaje objeto : objetos) {
             AlumnoObjetoAprendizajePK pk2 = new AlumnoObjetoAprendizajePK(alumno, objeto);
             AlumnoObjetoAprendizaje alumnoObjeto = (AlumnoObjetoAprendizaje) currentSession().get(AlumnoObjetoAprendizaje.class, pk2);
             if (alumnoObjeto == null) {
                 alumnoObjeto = new AlumnoObjetoAprendizaje(alumno, objeto);
                 currentSession().save(alumnoObjeto);
                 currentSession().flush();
             }
             for (Contenido contenido : objeto.getContenidos()) {
                 log.debug("Cargando contenido {} del objeto {}", contenido, objeto);
                 AlumnoContenidoPK pk = new AlumnoContenidoPK(alumno, contenido);
                 AlumnoContenido alumnoContenido = (AlumnoContenido) currentSession().get(AlumnoContenido.class, pk);
                 if (alumnoContenido == null) {
                     alumnoContenido = new AlumnoContenido(alumno, contenido);
                     currentSession().save(alumnoContenido);
                     currentSession().flush();
                 }
                 if (contenidoId == contenido.getId() && terminado) {
                     this.asignaContenido(cursoId, alumnoContenido, contenido, themeDisplay);
                     contenido.setActivo(true);
                     noAsignado = false;
                     activo = true;
                     log.debug("Validando si ha sido iniciado {}", alumnoContenido.getIniciado());
                     if (alumnoContenido.getIniciado() == null) {
                         alumnoContenido.setIniciado(fecha);
                         currentSession().update(alumnoContenido);
 
                         if (alumnoObjeto.getIniciado() == null) {
                             alumnoObjeto.setIniciado(fecha);
                             currentSession().update(alumnoObjeto);
                         }
 
                         currentSession().flush();
                     }
                 }
                 if (alumnoContenido.getTerminado() == null) {
                     terminado = false;
                 }
                 contenido.setAlumno(alumnoContenido);
             }
         }
         if (noAsignado) {
             for (ObjetoAprendizaje objeto : objetos) {
                 boolean bandera = true;
                 AlumnoObjetoAprendizajePK pk2 = new AlumnoObjetoAprendizajePK(alumno, objeto);
                 AlumnoObjetoAprendizaje alumnoObjeto = (AlumnoObjetoAprendizaje) currentSession().get(AlumnoObjetoAprendizaje.class, pk2);
                 if (alumnoObjeto == null) {
                     alumnoObjeto = new AlumnoObjetoAprendizaje(alumno, objeto);
                     currentSession().save(alumnoObjeto);
                     currentSession().flush();
                 }
                 for (Contenido contenido : objeto.getContenidos()) {
                     AlumnoContenidoPK pk = new AlumnoContenidoPK(alumno, contenido);
                     AlumnoContenido alumnoContenido = (AlumnoContenido) currentSession().get(AlumnoContenido.class, pk);
                     if (alumnoContenido == null) {
                         alumnoContenido = new AlumnoContenido(alumno, contenido);
                         currentSession().save(alumnoContenido);
                         currentSession().flush();
                     }
                     if (bandera && alumnoContenido.getTerminado() == null && !activo) {
                         this.asignaContenido(cursoId, alumnoContenido, contenido, themeDisplay);
                         contenido.setActivo(bandera);
                         bandera = false;
                         activo = true;
                         noAsignado = false;
                         if (alumnoContenido.getIniciado() == null) {
                             alumnoContenido.setIniciado(fecha);
                             currentSession().update(alumnoContenido);
 
                             if (alumnoObjeto.getIniciado() == null) {
                                 alumnoObjeto.setIniciado(fecha);
                                 currentSession().update(alumnoObjeto);
                             }
 
                             currentSession().flush();
                         }
                     }
                     contenido.setAlumno(alumnoContenido);
                 }
             }
         }
         if (noAsignado) {
             for (ObjetoAprendizaje objeto : objetos) {
                 boolean bandera = true;
                 AlumnoObjetoAprendizajePK pk2 = new AlumnoObjetoAprendizajePK(alumno, objeto);
                 AlumnoObjetoAprendizaje alumnoObjeto = (AlumnoObjetoAprendizaje) currentSession().get(AlumnoObjetoAprendizaje.class, pk2);
                 if (alumnoObjeto == null) {
                     alumnoObjeto = new AlumnoObjetoAprendizaje(alumno, objeto);
                     currentSession().save(alumnoObjeto);
                     currentSession().flush();
                 }
                 for (Contenido contenido : objeto.getContenidos()) {
                     AlumnoContenidoPK pk = new AlumnoContenidoPK(alumno, contenido);
                     AlumnoContenido alumnoContenido = (AlumnoContenido) currentSession().get(AlumnoContenido.class, pk);
                     if (alumnoContenido == null) {
                         alumnoContenido = new AlumnoContenido(alumno, contenido);
                         currentSession().save(alumnoContenido);
                         currentSession().flush();
                     }
                     if (bandera && !activo) {
                         this.asignaContenido(cursoId, alumnoContenido, contenido, themeDisplay);
                         contenido.setActivo(bandera);
                         alumnoContenido.setIniciado(fecha);
                         currentSession().update(alumnoContenido);
 
                         if (alumnoObjeto.getIniciado() == null) {
                             alumnoObjeto.setIniciado(fecha);
                             currentSession().update(alumnoObjeto);
                         }
 
                         currentSession().flush();
                         bandera = false;
                         activo = true;
                     }
                     contenido.setAlumno(alumnoContenido);
                 }
             }
         }
         return objetos;
     }
 
     @Override
     public List<ObjetoAprendizaje> objetosAlumnoSiguiente(Long cursoId, Long alumnoId, ThemeDisplay themeDisplay) {
         log.debug("Obteniendo siguiente contenido curso {} para el alumno {}", cursoId, alumnoId);
 
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         log.debug("{}", curso);
         Alumno alumno = (Alumno) currentSession().load(Alumno.class, alumnoId);
         log.debug("{}", alumno);
         AlumnoCursoPK alumnoCursoPK = new AlumnoCursoPK(alumno, curso);
         AlumnoCurso ac = (AlumnoCurso) currentSession().get(AlumnoCurso.class, alumnoCursoPK);
         ac.setUltimoAcceso(new Date());
         currentSession().update(ac);
         currentSession().flush();
 
         List<ObjetoAprendizaje> objetos = curso.getObjetos();
         boolean noAsignado = true;
         boolean activo = false;
         Date fecha = new Date();
         for (ObjetoAprendizaje objeto : objetos) {
             boolean bandera = true;
             AlumnoObjetoAprendizajePK pk2 = new AlumnoObjetoAprendizajePK(alumno, objeto);
             AlumnoObjetoAprendizaje alumnoObjeto = (AlumnoObjetoAprendizaje) currentSession().get(AlumnoObjetoAprendizaje.class, pk2);
             if (alumnoObjeto == null) {
                 alumnoObjeto = new AlumnoObjetoAprendizaje(alumno, objeto);
                 currentSession().save(alumnoObjeto);
                 currentSession().flush();
             }
             for (Contenido contenido : objeto.getContenidos()) {
                 log.debug("Cargando contenido {} del objeto {}", contenido, objeto);
                 AlumnoContenidoPK pk = new AlumnoContenidoPK(alumno, contenido);
                 AlumnoContenido alumnoContenido = (AlumnoContenido) currentSession().get(AlumnoContenido.class, pk);
                 if (alumnoContenido == null) {
                     alumnoContenido = new AlumnoContenido(alumno, contenido);
                     currentSession().save(alumnoContenido);
                     currentSession().flush();
                 }
                 if (bandera && alumnoContenido.getTerminado() == null && !activo) {
                     if (alumnoContenido.getIniciado() == null) {
                         this.asignaContenido(cursoId, alumnoContenido, contenido, themeDisplay);
                         contenido.setActivo(bandera);
                         activo = true;
                         alumnoContenido.setIniciado(fecha);
                         currentSession().update(alumnoContenido);
 
                         if (alumnoObjeto.getIniciado() == null) {
                             alumnoObjeto.setIniciado(fecha);
                             currentSession().update(alumnoObjeto);
                         }
 
                         currentSession().flush();
                         bandera = false;
                         noAsignado = false;
                     } else {
                         alumnoContenido.setTerminado(fecha);
                         currentSession().update(alumnoContenido);
                         currentSession().flush();
                     }
                 }
                 contenido.setAlumno(alumnoContenido);
             }
             if (!activo) {
                 alumnoObjeto.setTerminado(fecha);
                 currentSession().update(alumnoObjeto);
             }
         }
         if (noAsignado) {
             log.debug("Asignando contenido");
             for (ObjetoAprendizaje objeto : objetos) {
                 boolean bandera = true;
                 for (Contenido contenido : objeto.getContenidos()) {
                     AlumnoContenidoPK pk = new AlumnoContenidoPK(alumno, contenido);
                     AlumnoContenido alumnoContenido = (AlumnoContenido) currentSession().get(AlumnoContenido.class, pk);
                     if (alumnoContenido == null) {
                         alumnoContenido = new AlumnoContenido(alumno, contenido);
                         currentSession().save(alumnoContenido);
                         currentSession().flush();
                     }
                     if (bandera && !activo) {
                         this.asignaContenido(cursoId, alumnoContenido, contenido, themeDisplay);
                         contenido.setActivo(bandera);
                         activo = true;
                         if (alumnoContenido.getIniciado() == null) {
                             alumnoContenido.setIniciado(fecha);
                         } else {
                             AlumnoCursoPK pk2 = new AlumnoCursoPK(alumno, curso);
                             AlumnoCurso alumnoCurso = (AlumnoCurso) currentSession().get(AlumnoCurso.class, pk2);
                             alumnoCurso.setEstatus(Constantes.CONCLUIDO);
                             alumnoCurso.setFechaConclusion(fecha);
                             currentSession().update(alumnoCurso);
 
                             AlumnoObjetoAprendizajePK pk3 = new AlumnoObjetoAprendizajePK(alumno, objeto);
                             AlumnoObjetoAprendizaje alumnoObjeto = (AlumnoObjetoAprendizaje) currentSession().get(AlumnoObjetoAprendizaje.class, pk3);
                             alumnoObjeto.setTerminado(fecha);
                             currentSession().update(alumnoObjeto);
 
                             currentSession().flush();
                         }
                         currentSession().update(alumnoContenido);
                         currentSession().flush();
                         bandera = false;
                     }
                     contenido.setAlumno(alumnoContenido);
                 }
             }
         }
         return objetos;
     }
 
     @Override
     public List<AlumnoCurso> alumnos(Long cursoId) {
         log.debug("Lista de alumnos del curso {}", cursoId);
 
         Query query = currentSession().createQuery("select a from AlumnoCurso a where a.id.curso.id = :cursoId");
         query.setLong("cursoId", cursoId);
         return query.list();
     }
 
     @Override
     public void inscribe(Long cursoId, Long alumnoId) {
         log.debug("Inscribe alumno {} a curso {}", alumnoId, cursoId);
 
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         Alumno alumno = (Alumno) currentSession().get(Alumno.class, alumnoId);
         if (alumno == null) {
             try {
                 User usuario = UserLocalServiceUtil.getUser(alumnoId);
                 alumno = new Alumno(usuario);
                 alumno.setComunidad(curso.getComunidadId());
                 currentSession().save(alumno);
                 currentSession().flush();
             } catch (PortalException | SystemException ex) {
                 log.error("No se pudo obtener el usuario", ex);
             }
         }
         AlumnoCursoPK pk = new AlumnoCursoPK(alumno, curso);
         AlumnoCurso alumnoCurso = (AlumnoCurso) currentSession().get(AlumnoCurso.class, pk);
         if (alumnoCurso == null) {
             alumnoCurso = new AlumnoCurso(pk, Constantes.INSCRITO);
             currentSession().save(alumnoCurso);
         } else {
             alumnoCurso.setEstatus(Constantes.INSCRITO);
             alumnoCurso.setFecha(new Date());
             currentSession().update(alumnoCurso);
         }
     }
 
     @Override
     public Map<String, Object> alumnos(Map<String, Object> params) {
         Long cursoId = (Long) params.get("cursoId");
         Query query = currentSession().createQuery("select a from AlumnoCurso a join fetch a.id.curso where a.id.curso.id = :cursoId");
         query.setLong("cursoId", cursoId);
         List<AlumnoCurso> alumnos = query.list();
         for (AlumnoCurso alumnoCurso : alumnos) {
             alumnoCurso.setSaldo(alumnoCurso.getId().getCurso().getPrecio());
         }
         params.put("alumnos", alumnos);
 
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         params.put("curso", curso);
         try {
             log.debug("Buscando usuarios en la empresa {}", params.get("companyId"));
             List<User> usuarios = UserLocalServiceUtil.getCompanyUsers((Long) params.get("companyId"), QueryUtil.ALL_POS, QueryUtil.ALL_POS);
             List<User> lista = new ArrayList<>();
             for (User user : usuarios) {
                 if (!user.isDefaultUser()) {
                     lista.add(user);
                 }
             }
             params.put("disponibles", lista);
         } catch (SystemException e) {
             log.error("No se pudo obtener lista de usuarios", e);
         }
 
         return params;
     }
 
     private void asignaContenido(Long cursoId, AlumnoContenido alumnoContenido, Contenido contenido, ThemeDisplay themeDisplay) {
         try {
             StringBuilder sb2 = new StringBuilder();
             sb2.append("admin");
             sb2.append(cursoId);
             sb2.append(contenido.getId());
             sb2.append(Constantes.SALT);
 
             JournalArticle ja;
             switch (contenido.getTipo()) {
                 case Constantes.ARTICULATE:
                     StringBuilder sb = new StringBuilder();
                     sb.append("<iframe src='/academia-portlet");
                     sb.append("/conteni2");
                     sb.append("/admin");
                     sb.append("/").append(cursoId);
                     sb.append("/").append(contenido.getId());
                     sb.append("/").append(DigestUtils.shaHex(sb2.toString()));
                     sb.append("/player.html");
                     sb.append("' style='width:100%;height:600px;'></iframe>");
                     contenido.setTexto(sb.toString());
                     break;
                 case Constantes.STORYLINE:
                     sb = new StringBuilder();
                     sb.append("<iframe src='/academia-portlet");
                     sb.append("/conteni2");
                     sb.append("/admin");
                     sb.append("/").append(cursoId);
                     sb.append("/").append(contenido.getId());
                     sb.append("/").append(DigestUtils.shaHex(sb2.toString()));
                     sb.append("/story.html");
                     sb.append("' style='width:100%;height:650px;'></iframe>");
                     contenido.setTexto(sb.toString());
                     break;
                 case Constantes.TEXTO:
                     ja = JournalArticleLocalServiceUtil.getArticle(contenido.getContenidoId());
                     if (ja != null) {
                         String texto = JournalArticleLocalServiceUtil.getArticleContent(ja.getGroupId(), ja.getArticleId(), "view", "" + themeDisplay.getLocale(), themeDisplay);
                         contenido.setTexto(texto);
                     }
                     break;
                 case Constantes.VIDEO:
                     log.debug("Buscando el video con el id {}", contenido.getContenidoId());
                     DLFileEntry fileEntry = DLFileEntryLocalServiceUtil.getDLFileEntry(contenido.getContenidoId());
                     if (fileEntry != null) {
                         StringBuilder videoLink = new StringBuilder();
                         videoLink.append("/documents/");
                         videoLink.append(fileEntry.getGroupId());
                         videoLink.append("/");
                         videoLink.append(fileEntry.getFolderId());
                         videoLink.append("/");
                         videoLink.append(fileEntry.getTitle());
                         contenido.setTexto(videoLink.toString());
                     }
                     break;
                 case Constantes.EXAMEN:
                     Examen examen = contenido.getExamen();
                     if (examen.getContenido() != null) {
                         ja = JournalArticleLocalServiceUtil.getArticle(examen.getContenido());
                         if (ja != null) {
                             String texto = JournalArticleLocalServiceUtil.getArticleContent(ja.getGroupId(), ja.getArticleId(), "view", "" + themeDisplay.getLocale(), themeDisplay);
                             contenido.setTexto(texto);
                         }
                     }
 
                     List<Pregunta> preguntas = new ArrayList<>();
                     for (Pregunta pregunta : examenDao.preguntas(examen.getId())) {
                         for (Respuesta respuesta : pregunta.getRespuestas()) {
                             if (respuesta.getContenido() != null) {
                                 ja = JournalArticleLocalServiceUtil.getArticle(respuesta.getContenido());
                                 if (ja != null) {
                                     String texto = JournalArticleLocalServiceUtil.getArticleContent(ja.getGroupId(), ja.getArticleId(), "view", "" + themeDisplay.getLocale(), themeDisplay);
                                     respuesta.setTexto(texto);
                                 }
                             } else {
                                 String texto = messages.getMessage("respuesta.requiere.texto", new String[]{respuesta.getNombre()}, themeDisplay.getLocale());
                                 respuesta.setTexto(texto);
                             }
                         }
                         if (pregunta.getContenido() != null) {
                             ja = JournalArticleLocalServiceUtil.getArticle(pregunta.getContenido());
                             if (ja != null) {
                                 String texto = JournalArticleLocalServiceUtil.getArticleContent(ja.getGroupId(), ja.getArticleId(), "view", "" + themeDisplay.getLocale(), themeDisplay);
                                 pregunta.setTexto(texto);
                             }
                         } else {
                             String texto = messages.getMessage("pregunta.requiere.texto", new String[]{pregunta.getNombre()}, themeDisplay.getLocale());
                             pregunta.setTexto(texto);
                         }
                         preguntas.add(pregunta);
                     }
                     if (preguntas.size() > 0) {
                         for (Pregunta pregunta : preguntas) {
                             log.debug("{} ||| {}", pregunta, pregunta.getTexto());
                         }
                         examen.setOtrasPreguntas(preguntas);
                     }
                     break;
             }
             log.debug("Validando si ha sido iniciado {}", alumnoContenido.getIniciado());
             if (alumnoContenido.getIniciado() == null) {
                 alumnoContenido.setIniciado(new Date());
                 currentSession().update(alumnoContenido);
                 currentSession().flush();
             }
         } catch (PortalException | SystemException e) {
             log.error("No se pudo obtener el texto del contenido", e);
         }
     }
 
     @Override
     public Examen obtieneExamen(Long examenId) {
         Examen examen = (Examen) currentSession().get(Examen.class, examenId);
         return examen;
     }
 
     @Override
     public Map<String, Object> califica(Map<String, String[]> params, ThemeDisplay themeDisplay, User usuario) {
         try {
             Examen examen = (Examen) currentSession().get(Examen.class, new Long(params.get("examenId")[0]));
             Integer totalExamen = 0;
             Integer totalUsuario = 0;
             Set<Pregunta> incorrectas = new LinkedHashSet<>();
             for (ExamenPregunta examenPregunta : examen.getPreguntas()) {
                 Pregunta pregunta = examenPregunta.getId().getPregunta();
                 log.debug("{}({}:{}) > Multiple : {} || Por pregunta : {}", new Object[]{pregunta.getNombre(), pregunta.getId(), examen.getId(), pregunta.getEsMultiple(), examenPregunta.getPorPregunta()});
                 if (pregunta.getEsMultiple() && examenPregunta.getPorPregunta()) {
                     // Cuando puede tener muchas respuestas y los puntos son por pregunta
                     log.debug("ENTRO 1");
                     totalExamen += examenPregunta.getPuntos();
                     String[] respuestas = params.get(pregunta.getId().toString());
                     List<String> correctas = new ArrayList<>();
                     if (respuestas.length == pregunta.getCorrectas().size()) {
                         boolean vaBien = true;
                         for (Respuesta correcta : pregunta.getCorrectas()) {
                             boolean encontre = false;
                             for (String respuesta : respuestas) {
                                 if (respuesta.equals(correcta.getId().toString())) {
                                     encontre = true;
                                     correctas.add(respuesta);
                                     break;
                                 }
                             }
                             if (!encontre) {
                                 vaBien = false;
                             }
                         }
                         if (vaBien) {
                             totalUsuario += examenPregunta.getPuntos();
                         } else {
                             // pon respuesta incorrecta
                             for (String respuestaId : respuestas) {
                                 if (!correctas.contains(respuestaId)) {
                                     Respuesta respuesta = (Respuesta) currentSession().get(Respuesta.class, new Long(respuestaId));
                                     JournalArticle ja = JournalArticleLocalServiceUtil.getArticle(respuesta.getContenido());
                                     if (ja != null) {
                                         String texto = JournalArticleLocalServiceUtil.getArticleContent(ja.getGroupId(), ja.getArticleId(), "view", "" + themeDisplay.getLocale(), themeDisplay);
                                         respuesta.setTexto(texto);
                                     }
                                     pregunta.getRespuestas().add(respuesta);
                                 }
                             }
                             JournalArticle ja = JournalArticleLocalServiceUtil.getArticle(pregunta.getContenido());
                             if (ja != null) {
                                 String texto = JournalArticleLocalServiceUtil.getArticleContent(ja.getGroupId(), ja.getArticleId(), "view", "" + themeDisplay.getLocale(), themeDisplay);
                                 pregunta.setTexto(texto);
                             }
                             incorrectas.add(pregunta);
                         }
                     }
                 } else {
                     // Cuando puede tener muchas respuestas pero los puntos son por respuesta
                     // Tambien cuando es una sola respuesta la correcta
                     log.debug("ENTRO 2");
                     String[] respuestas = params.get(pregunta.getId().toString());
                     List<String> correctas = new ArrayList<>();
                     if (respuestas.length <= pregunta.getCorrectas().size()) {
                         log.debug("ENTRO 3");
                         respuestasLoop:
                         for (Respuesta correcta : pregunta.getCorrectas()) {
                             log.debug("Pregunta: {} | Examen: {} | Alumno: {}", new Object[] {examenPregunta.getPuntos(), totalExamen, totalUsuario});
                             totalExamen += examenPregunta.getPuntos();
                             for (String respuesta : respuestas) {
                                 if (respuesta.equals(correcta.getId().toString())) {
                                     totalUsuario += examenPregunta.getPuntos();
                                     correctas.add(respuesta);
                                     continue respuestasLoop;
                                 }
                             }
                         }
                         if (correctas.size() < pregunta.getCorrectas().size()) {
                             // pon respuesta incorrecta
                             for (String respuestaId : respuestas) {
                                 if (!correctas.contains(respuestaId)) {
                                     Respuesta respuesta = (Respuesta) currentSession().get(Respuesta.class, new Long(respuestaId));
                                     JournalArticle ja = JournalArticleLocalServiceUtil.getArticle(respuesta.getContenido());
                                     if (ja != null) {
                                         String texto = JournalArticleLocalServiceUtil.getArticleContent(ja.getGroupId(), ja.getArticleId(), "view", "" + themeDisplay.getLocale(), themeDisplay);
                                         respuesta.setTexto(texto);
                                     }
                                     pregunta.getRespuestas().add(respuesta);
                                 }
                             }
 
                             JournalArticle ja = JournalArticleLocalServiceUtil.getArticle(pregunta.getContenido());
                             if (ja != null) {
                                 String texto = JournalArticleLocalServiceUtil.getArticleContent(ja.getGroupId(), ja.getArticleId(), "view", "" + themeDisplay.getLocale(), themeDisplay);
                                 pregunta.setTexto(texto);
                             }
                             incorrectas.add(pregunta);
                         }
                     }
                 }
                 log.debug("Pregunta {} : Respuesta {} : Usuario {}", new Object[]{pregunta.getId(), pregunta.getCorrectas(), params.get(pregunta.getId().toString())});
             }
 
             Map<String, Object> resultados = new HashMap<>();
             resultados.put("examen", examen);
             resultados.put("totalExamen", totalExamen);
             resultados.put("totalUsuario", totalUsuario);
             resultados.put("totales", new String[]{totalUsuario.toString(), totalExamen.toString(), examen.getPuntos().toString()});
             if (examen.getPuntos() != null && totalUsuario < examen.getPuntos()) {
                 resultados.put("messageTitle", "desaprobado");
                 resultados.put("messageType", "alert-error");
                 Long contenidoId = new Long(params.get("contenidoId")[0]);
                 Alumno alumno = (Alumno) currentSession().load(Alumno.class, usuario.getUserId());
                 Contenido contenido = (Contenido) currentSession().load(Contenido.class, contenidoId);
                 AlumnoContenidoPK pk = new AlumnoContenidoPK(alumno, contenido);
                 AlumnoContenido alumnoContenido = (AlumnoContenido) currentSession().get(AlumnoContenido.class, pk);
                 if (alumnoContenido != null && alumnoContenido.getTerminado() != null) {
                     alumnoContenido.setTerminado(null);
                     currentSession().update(alumnoContenido);
                     currentSession().flush();
                 }
             } else {
                 resultados.put("messageTitle", "aprobado");
                 resultados.put("messageType", "alert-success");
                 for (String key : params.keySet()) {
                     log.debug("{} : {}", key, params.get(key));
                 }
                 Long contenidoId = new Long(params.get("contenidoId")[0]);
                 Alumno alumno = (Alumno) currentSession().load(Alumno.class, usuario.getUserId());
                 Contenido contenido = (Contenido) currentSession().load(Contenido.class, contenidoId);
                 AlumnoContenidoPK pk = new AlumnoContenidoPK(alumno, contenido);
                 AlumnoContenido alumnoContenido = (AlumnoContenido) currentSession().get(AlumnoContenido.class, pk);
                 if (alumnoContenido != null) {
                     alumnoContenido.setTerminado(new Date());
                     currentSession().update(alumnoContenido);
                     currentSession().flush();
                 }
             }
             if (incorrectas.size() > 0) {
                 resultados.put("incorrectas", incorrectas);
             }
             return resultados;
         } catch (PortalException | SystemException e) {
             log.error("No se pudo calificar el examen", e);
         }
         return null;
     }
 
     @Override
     public Boolean haConcluido(Long alumnoId, Long cursoId) {
         Curso curso = (Curso) currentSession().load(Curso.class, cursoId);
         Alumno alumno = (Alumno) currentSession().load(Alumno.class, alumnoId);
         AlumnoCursoPK pk = new AlumnoCursoPK(alumno, curso);
         AlumnoCurso alumnoCurso = (AlumnoCurso) currentSession().get(AlumnoCurso.class, pk);
         boolean resultado = false;
         if (alumnoCurso.getEstatus().equals(Constantes.CONCLUIDO)) {
             resultado = true;
         }
         return resultado;
     }
 
     @Override
     public List<AlumnoCurso> obtieneCursos(Long alumnoId) {
         log.debug("Buscando los cursos del alumno {}", alumnoId);
         Query query = currentSession().createQuery("select ac from AlumnoCurso ac "
                 + "join fetch ac.id.curso "
                 + "where ac.id.alumno.id = :alumnoId "
                 + "order by fecha desc");
         query.setLong("alumnoId", alumnoId);
         return query.list();
     }
 
     @Override
     public AlumnoCurso obtieneAlumnoCurso(Long alumnoId, Long cursoId) {
         log.debug("Buscando el curso con {} y {}", alumnoId, cursoId);
         Query query = currentSession().createQuery("select ac from AlumnoCurso ac "
                 + "join fetch ac.id.alumno "
                 + "join fetch ac.id.curso "
                 + "where ac.id.alumno.id = :alumnoId "
                 + "and ac.id.curso.id = :cursoId");
         query.setLong("alumnoId", alumnoId);
         query.setLong("cursoId", cursoId);
         AlumnoCurso alumnoCurso = (AlumnoCurso) query.uniqueResult();
         if (alumnoCurso != null) {
             alumnoCurso.setUltimoAcceso(new Date());
             currentSession().save(alumnoCurso);
             currentSession().flush();
         }
         log.debug("Regresando el alumnoCurso {}", alumnoCurso);
 
         return alumnoCurso;
     }
 
     public void asignaReporte(Reporte reporte, Curso curso) {
         reporte.setNombre(curso.getCodigo());
         reporte.setCurso(curso);
         Date fecha = new Date();
         reporte.setFechaModificacion(fecha);
         reporte.setFechaCreacion(fecha);
         currentSession().save(reporte);
     }
 
     public void modificaReporte(Reporte reporte, Curso curso) {
         Query query = currentSession().createQuery("select r from Reporte r where r.curso.id = :cursoId");
         query.setLong("cursoId", curso.getId());
         Reporte otro = (Reporte) query.uniqueResult();
         if (otro != null) {
             otro.setCompilado(reporte.getCompilado());
             otro.setNombre(curso.getCodigo());
             Date fecha = new Date();
             otro.setFechaModificacion(fecha);
             currentSession().update(otro);
         } else {
             this.asignaReporte(reporte, curso);
         }
     }
 
     @Override
     public JasperReport obtieneReporte(Long cursoId) {
         Query query = currentSession().createQuery("select r from Reporte r where r.curso.id = :cursoId");
         query.setLong("cursoId", cursoId);
         Reporte reporte = (Reporte) query.uniqueResult();
         JasperReport jr = reporte.getReporte();
         return jr;
     }
 
     @Override
     public Map<String, Object> todosAlumnos(Map<String, Object> params) {
         MathContext mc = new MathContext(16, RoundingMode.HALF_UP);
         Long comunidadId = (Long) params.get("comunidadId");
         Query query = currentSession().createQuery("select a from AlumnoCurso a "
                 + "join fetch a.id.alumno "
                 + "join fetch a.id.curso "
                 + "where a.id.curso.comunidadId = :comunidadId");
         query.setLong("comunidadId", comunidadId);
         List<AlumnoCurso> lista = query.list();
         Map<String, AlumnoCurso> map = new TreeMap<>();
 
         for (AlumnoCurso alumnoCurso : lista) {
             AlumnoCurso a = map.get(alumnoCurso.getAlumno().getUsuario());
             if (a == null) {
                 a = alumnoCurso;
                 try {
                     boolean cambio = false;
                     User user = UserLocalServiceUtil.getUser(alumnoCurso.getId().getAlumno().getId());
                     Alumno alumno = a.getAlumno();
                     if (!alumno.getCorreo().equals(user.getEmailAddress())) {
                         alumno.setCorreo(user.getEmailAddress());
                         cambio = true;
                     }
                     if (!alumno.getNombreCompleto().equals(user.getFullName())) {
                         alumno.setNombreCompleto(user.getFullName());
                         cambio = true;
                     }
                     if (!alumno.getUsuario().equals(user.getScreenName())) {
                         alumno.setUsuario(user.getScreenName());
                         cambio = true;
                     }
                     if (cambio) {
                         currentSession().update(alumno);
                     }
                 } catch (PortalException | SystemException ex) {
                     log.error("No se pudo obtener al usuario", ex);
                 }
             }
             StringBuilder sb = new StringBuilder();
             if (StringUtils.isNotBlank(a.getCursos())) {
                 sb.append(a.getCursos());
                 sb.append(", ");
             }
             sb.append(alumnoCurso.getCurso().getCodigo());
             a.setCursos(sb.toString());
             a.setSaldo(a.getSaldo().add(alumnoCurso.getCurso().getPrecio(), mc).setScale(2, RoundingMode.HALF_UP));
             map.put(alumnoCurso.getAlumno().getUsuario(), a);
         }
 
         params.put("alumnos", map.values());
 
         return params;
     }
 
     @Override
     public void bajaAlumno(Long alumnoId, Long cursoId) {
         log.debug("Baja a alumno {} de curso {}", alumnoId, cursoId);
         Curso curso = (Curso) currentSession().load(Curso.class, cursoId);
         Alumno alumno = (Alumno) currentSession().load(Alumno.class, alumnoId);
         AlumnoCursoPK pk = new AlumnoCursoPK(alumno, curso);
         AlumnoCurso alumnoCurso = (AlumnoCurso) currentSession().load(AlumnoCurso.class, pk);
         currentSession().delete(alumnoCurso);
     }
 
     @Override
     public void asignaCorreo(Curso curso) {
         Query query = currentSession().createQuery("update Curso set correoId = :correoId where id = :id and version = :version");
         query.setLong("correoId", curso.getCorreoId());
         query.setLong("id", curso.getId());
         query.setLong("version", curso.getVersion());
         query.executeUpdate();
     }
 
     @Override
     public Salon obtieneSalon(Long cursoId) {
         log.debug("Obtiene salon por el curso {}", cursoId);
         Query query = currentSession().createQuery("select s from Salon s where s.curso.id = :cursoId");
         query.setLong("cursoId", cursoId);
         return (Salon) query.uniqueResult();
     }
 
     @Override
     public Salon creaSalon(Salon salon) {
         log.debug("Creando salon {}", salon);
         currentSession().save(salon);
         return salon;
     }
 
     @Override
     public Salon obtieneSalonPorId(Long salonId) {
         log.debug("Obtiene salon por su id {}", salonId);
         Salon salon = (Salon) currentSession().get(Salon.class, salonId);
         return salon;
     }
 
     @Override
     public Salon actualizaSalon(Salon salon) {
         log.debug("Actualizando el salon {}", salon);
         currentSession().update(salon);
         return salon;
     }
 
     @Override
     public void eliminaSalon(Salon salon) {
         log.debug("Eliminando salon {}", salon);
         currentSession().delete(salon);
     }
 
     @Override
     public void actualizaObjetos(Long cursoId, Long[] objetos) {
         log.debug("Actualizando objetos {} del curso {}", objetos, cursoId);
         Curso curso = (Curso) currentSession().get(Curso.class, cursoId);
         curso.getObjetos().clear();
         currentSession().update(curso);
         currentSession().flush();
         if (objetos != null) {
             for (Long objetoId : objetos) {
                 curso.getObjetos().add((ObjetoAprendizaje) currentSession().load(ObjetoAprendizaje.class, objetoId));
             }
             currentSession().update(curso);
             currentSession().flush();
         }
     }
 
     @Override
     public List<ObjetoAprendizaje> buscaObjetos(Long cursoId, String filtro) {
         Query query = currentSession().createQuery("select comunidadId from Curso where id = :cursoId");
         query.setLong("cursoId", cursoId);
         Long comunidadId = (Long) query.uniqueResult();
 
         query = currentSession().createQuery("select o.id from Curso c inner join c.objetos as o where c.id = :cursoId");
         query.setLong("cursoId", cursoId);
         List<Long> ids = query.list();
 
         Criteria criteria = currentSession().createCriteria(ObjetoAprendizaje.class);
         if (comunidadId != 23461L) {
             criteria.add(Restrictions.eq("comunidadId", comunidadId));
         }
         if (ids != null && ids.size() > 0) {
             criteria.add(Restrictions.not(Restrictions.in("id", ids)));
         }
         Disjunction propiedades = Restrictions.disjunction();
         propiedades.add(Restrictions.ilike("codigo", filtro, MatchMode.ANYWHERE));
         propiedades.add(Restrictions.ilike("nombre", filtro, MatchMode.ANYWHERE));
         criteria.add(propiedades);
         criteria.addOrder(Order.asc("codigo"));
         return criteria.list();
     }
 
     @Override
     public List<Map> contenidos(Long alumnoId, Long cursoId) {
         String s = "SELECT o.nombre as objetoNombre, ao.iniciado as objetoIniciado, ao.terminado as objetoTerminado, c.nombre as contenidoNombre, ac.iniciado as contenidoIniciado, ac.terminado as contenidoTerminado "
                 + "FROM aca_cursos_aca_objetos co, aca_objetos_aca_contenidos oc, aca_alumno_objeto ao, aca_alumno_contenido ac, aca_objetos o, aca_contenidos c "
                 + "where co.cursos_id = :cursoId "
                 + "and co.objetos_id = oc.objetos_id "
                 + "and co.objetos_id = ao.objeto_id "
                 + "and ao.alumno_id = ac.alumno_id "
                 + "and oc.contenidos_id = ac.contenido_id "
                 + "and co.objetos_id = o.id "
                 + "and ac.contenido_id = c.id "
                 + "and ao.alumno_id = :alumnoId "
                 + "order by co.orden, oc.orden";
         SQLQuery query = currentSession().createSQLQuery(s);
         query.setLong("cursoId", cursoId);
         query.setLong("alumnoId", alumnoId);
         query.addScalar("objetoNombre", STRING);
         query.addScalar("objetoIniciado", TIMESTAMP);
         query.addScalar("objetoTerminado", TIMESTAMP);
         query.addScalar("contenidoNombre", STRING);
         query.addScalar("contenidoIniciado", TIMESTAMP);
         query.addScalar("contenidoTerminado", TIMESTAMP);
         query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
         return query.list();
     }
 }
