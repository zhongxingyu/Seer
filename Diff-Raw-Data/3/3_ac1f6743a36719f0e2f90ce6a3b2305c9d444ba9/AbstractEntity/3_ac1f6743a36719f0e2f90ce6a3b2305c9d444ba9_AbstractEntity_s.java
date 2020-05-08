 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.db.data;
 
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Transient;
 
 @MappedSuperclass
 public abstract class AbstractEntity implements Cloneable {
 	private Long id;
 
 	@Id
 	@GeneratedValue
 	@Column(nullable = false)
 	public Long getId() {
 		return id;
 	}
 
 	@SuppressWarnings("unused")
 	private void setId(long id) {
 		this.id = id;
 	}
 
 	@Transient
 	public boolean isTransient() {
 		return getId() == null;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o == null) {
 			return false;
 		}
 
 		if (o == this) {
 			return true;
 		}
 
 		if (!getClass().equals(o.getClass())) {
 			return false;
 		}
 
 		AbstractEntity e = (AbstractEntity)o;
 
 		if (isTransient() || e.isTransient()) {
 			return false;
 		}
 
 		return getId().equals(e.getId());
 	}
 
 	@Override
 	public int hashCode() {
 		return (int)(id ^ (id >>> 32));
 	}
 
 	@Override
 	public Object clone() {
 		try {
 			return super.clone();
 		} catch (CloneNotSupportedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
