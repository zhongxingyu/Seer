 package de.zib.gndms.model.gorfx;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import de.zib.gndms.model.common.PermissionInfo;
 import de.zib.gndms.model.common.PersistentContract;
 import de.zib.gndms.model.common.TimedGridResource;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.stuff.copy.Copier;
 import de.zib.gndms.stuff.copy.CopyMode;
 import de.zib.gndms.stuff.copy.Copyable;
 import de.zib.gndms.stuff.mold.Mold;
 import de.zib.gndms.stuff.mold.Molder;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * ThingAMagic.
  * 
  * @author  try ste fan pla nti kow zib
  * @version $Id$ 
  *
  * User: stepn Date: 05.09.2008 Time: 13:41:58
  */
 @MappedSuperclass
 @Copyable(CopyMode.MOLD)
 public abstract class AbstractTask extends TimedGridResource {
 
     private OfferType offerType;
     
     private String description;
 
     private byte[] serializedCredential;
 
     private PersistentContract contract;
 
     private boolean broken = false;
 
     private TaskState state = TaskState.CREATED;
 
     private boolean done = false;
     
     private int progress = 0;
 
     private int maxProgress = 100;
 
     private Serializable orq;
 
 
     private String faultString;
     /**
      * Payload depending on state, either task results or a detailed task failure
      **/
     private Serializable data;
 
 
     private String wid;
 
     private PermissionInfo permissions;
 
     private List<SubTask> subTasks = new ArrayList<SubTask>();
 
 
     public void transit(final TaskState newState) {
         final @NotNull TaskState goalState = newState == null ? getState() : newState;
         final @NotNull TaskState transitState = getState().transit(goalState);
         assert transitState != null;
         setState(transitState);
     }
 
 
 
     public void fail (final @NotNull Exception e) {
         setState(getState().transit(TaskState.FAILED));
         setFaultString(e.getMessage());
         setData(e);
         setProgress(0);
     }
 
     public void finish(final Serializable result) {
         setState(getState().transit(TaskState.FINISHED));
         setFaultString("");
         setData(result);
         setProgress( getMaxProgress() );
     }
 
     public void add ( SubTask st ) {
         if( getSubTasks() == null );
             setSubTasks( new LinkedList<SubTask>( ) );
         
         getSubTasks().add( st );
     }
 
 
     // TODO veryify "(D) this"
     @SuppressWarnings({"unchecked"})
     public <D> Molder<D> molder(@NotNull final Class<D> moldedClazz) {
         return Mold.newMolderProxy( (Class<D>) getClass(), (D) this, moldedClazz);
     }
 
     public void mold(final @NotNull AbstractTask instance) {
         instance.setId( getId() );
         instance.setTerminationTime( getTerminationTime() );
         instance.setDescription( getDescription() );
         instance.setWid( getWid() );
         instance.setFaultString( getFaultString() );
         instance.setDone( isDone() );
         instance.setState( getState() );
         instance.setBroken( isBroken() );
         instance.setProgress( getProgress() );
         instance.setMaxProgress( getMaxProgress() );
         instance.setContract( Copier.copy(false, getContract() ) );
         /* shallow therefore needs refresh */
         instance.setOfferType( getOfferType() );
         instance.setOrq( Copier.copySerializable( getOrq() ) );
         instance.setData( Copier.copySerializable( getData() ) );
         instance.setPermissions( Copier.copyViaConstructor( getPermissions() ) );
         if ( getSubTasks() == null)
            instance.setSubTasks( null );
         else {
            instance.setSubTasks( new LinkedList<SubTask>() );
            instance.getSubTasks().addAll( getSubTasks() );
         }
     }
 
     public void refresh(final @NotNull EntityManager em) {
         if ( getOfferType() != null)
             setOfferType( em.find(OfferType.class, getOfferType().getOfferTypeKey()) );
     }
 
 
     /* Nullable for testing purposes */
     @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="offerTypeKey", nullable=false, updatable=false, columnDefinition="VARCHAR")
     public OfferType getOfferType() {
         return offerType;
     }
 
 
     @Column(name="descr", nullable=false, updatable=false, columnDefinition="VARCHAR", length=5000)
     public String getDescription() {
         return description;
     }
 
 
     @Column(name="cred", nullable=true, updatable=true, columnDefinition="BLOB")
     @Lob
     public byte[] getSerializedCredential() {
         return serializedCredential;
     }
 
 
     /* always this.tod >= this.validity */
     @Embedded
     @AttributeOverrides({
         @AttributeOverride(name="accepted", column=@Column(name="accepted", nullable=false, updatable=false)),
         @AttributeOverride(name="deadline", column=@Column(name="deadline", nullable=true, updatable=false)),
         @AttributeOverride(name="resultValidity", column=@Column(name="validity", nullable=true, updatable=false)) 
     })
     public PersistentContract getContract() {
         return contract;
     }
 
 
     @Column(name="broken", updatable=true)
     public boolean isBroken() {
         return broken;
     }
 
 
     @Enumerated(EnumType.STRING)
     @Column(name="state", nullable=false, updatable=true)
     public TaskState getState() {
         return state;
     }
@Column(name = "done", nullable=false, updatable=true) public boolean isDone() { return done; }
 
 
     @Column(name = "progress", nullable=false, updatable=true)
     public int getProgress() {
         return progress;
     }
 
 
     @Column(name="max_progress", nullable=false, updatable=false)
     public int getMaxProgress() {
         return maxProgress;
     }
 
 
     @Column(name="orq", nullable=false, updatable=true)
     @Basic
     public Serializable getOrq() {
         return orq;
     }
 
 
    @Column( name="fault", nullable=true, updatable=true )
    @Lob
     public String getFaultString() {
         return faultString;
     }
 
 
     @Column(name="data", nullable=true, updatable=true)
     @Basic 
     public Serializable getData() {
         return data;
     }
 
 
     @Column(name="wid", nullable=true, updatable=false)
     @Basic
     public String getWid() {
         return wid;
     }
 
 
     @Embedded
     public PermissionInfo getPermissions() {
         return permissions;
     }
 
 
     @OneToMany( targetEntity=SubTask.class, cascade={CascadeType.REMOVE}, fetch=FetchType.EAGER )
     public List<SubTask> getSubTasks() {
         return subTasks;
     }
 
 
     public void setOfferType( OfferType offerType ) {
         this.offerType = offerType;
     }
 
 
     public void setDescription( String description ) {
         this.description = description;
     }
 
 
     public void setSerializedCredential( byte[] serializedCredential ) {
         this.serializedCredential = serializedCredential;
     }
 
 
     public void setContract( PersistentContract contract ) {
         this.contract = contract;
     }
 
 
     public void setBroken( boolean broken ) {
         this.broken = broken;
     }
 
 
     public void setState( TaskState state ) {
         this.state = state;
     }
 
 
     public void setDone( boolean done ) {
         this.done = done;
     }
 
 
     public void setProgress( int progress ) {
         this.progress = progress;
     }
 
 
     public void setMaxProgress( int maxProgress ) {
         this.maxProgress = maxProgress;
     }
 
 
     public void setOrq( Serializable orq ) {
         this.orq = orq;
     }
 
 
     public void setFaultString( String faultString ) {
         this.faultString = faultString;
     }
 
 
     public void setData( Serializable data ) {
         this.data = data;
     }
 
 
     public void setWid( String wid ) {
         this.wid = wid;
     }
 
 
     public void setPermissions( PermissionInfo permissions ) {
         this.permissions = permissions;
     }
 
 
     public void setSubTasks( List<SubTask> subTasks ) {
         this.subTasks = subTasks;
     }
 }
