 package br.edu.eseg.brproject.control.transactions;
 
 import javax.ejb.Local;
 
 import br.edu.eseg.brproject.model.Recurso;
 import br.edu.eseg.brproject.model.Tarefa;
 
 @Local
 public interface GanttTx {
 
 	public Long createRecurso(Recurso r);
 
 	public void saveTarefa(Tarefa tarefa);
 
 	public void excluirTarefa(Tarefa tarefa);
 
 	public void destroy();
 
 }
